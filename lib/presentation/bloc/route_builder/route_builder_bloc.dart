import 'dart:math';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';
import '../../../domain/entities/waypoint.dart';
import '../../../domain/repositories/local_repository.dart';
import '../../../domain/usecases/build_plan.dart';
import '../../../domain/usecases/build_route.dart';
import '../../../domain/usecases/export_plan.dart';
import '../../../domain/usecases/suggest_waypoints.dart';

part 'route_builder_event.dart';
part 'route_builder_state.dart';

const _kSportKey = 'sport_pref_v1';
const _kUnitKey  = 'unit_pref_v1';

class RouteBuilderBloc extends Bloc<RouteBuilderEvent, RouteBuilderState> {
  final BuildRoute _buildRoute;
  final SuggestWaypoints _suggestWaypoints;
  final BuildPlan _buildPlan;
  final ExportPlan _exportPlan;
  final LocalRepository _local;
  final SharedPreferences _prefs;

  RouteBuilderBloc({
    required BuildRoute buildRoute,
    required SuggestWaypoints suggestWaypoints,
    required BuildPlan buildPlan,
    required ExportPlan exportPlan,
    required LocalRepository local,
    required SharedPreferences prefs,
  })  : _buildRoute = buildRoute,
        _suggestWaypoints = suggestWaypoints,
        _buildPlan = buildPlan,
        _exportPlan = exportPlan,
        _local = local,
        _prefs = prefs,
        super(RouteBuilderState(
          startTime: DateTime.now().copyWith(hour: 7, minute: 0, second: 0, millisecond: 0),
          // Restore persisted sport & unit preferences on launch
          sport: SportType.values.firstWhere(
            (s) => s.name == prefs.getString(_kSportKey),
            orElse: () => SportType.hiking,
          ),
          unit: DistanceUnit.values.firstWhere(
            (u) => u.name == prefs.getString(_kUnitKey),
            orElse: () => DistanceUnit.kilometers,
          ),
        )) {
    on<SetOrigin>(_onSetOrigin);
    on<SetDestination>(_onSetDestination);
    on<SetSport>(_onSetSport);
    on<SetUnit>(_onSetUnit);
    on<AddViaPoint>(_onAddVia);
    on<RemoveViaPoint>(_onRemoveVia);
    on<RequestSuggestions>(_onRequestSuggestions);
    on<AcceptSuggestions>(_onAcceptSuggestions);
    on<GenerateRoute>(_onGenerateRoute);
    on<SetDays>(_onSetDays);
    on<SetSpeed>(_onSetSpeed);
    on<SetStartTime>(_onSetStartTime);
    on<ToggleBreak>(_onToggleBreak);
    on<BuildPlanEvent>(_onBuildPlan);
    on<ExportEvent>(_onExport);
    on<GoToStep>(_onGoToStep);
    on<ResetAll>(_onReset);
    on<MapControllerReady>(_onMapReady);
  }

  void _onMapReady(MapControllerReady e, Emitter<RouteBuilderState> emit) {
    emit(state.copyWith(mapController: e.controller));
  }

  Future<void> _fitCamera(List<Location> locations) async {
    final ctrl = state.mapController;
    if (ctrl == null || locations.isEmpty) return;

    if (locations.length == 1) {
      await ctrl.flyTo(
        CameraOptions(
          center: Point(coordinates: Position(locations[0].lng, locations[0].lat)),
          zoom: 11,
        ),
        MapAnimationOptions(duration: 900),
      );
      return;
    }

    double minLat = locations[0].lat, maxLat = locations[0].lat;
    double minLng = locations[0].lng, maxLng = locations[0].lng;
    for (final l in locations) {
      minLat = min(minLat, l.lat);
      maxLat = max(maxLat, l.lat);
      minLng = min(minLng, l.lng);
      maxLng = max(maxLng, l.lng);
    }

    await ctrl.cameraForCoordinatesPadding(
      [
        Point(coordinates: Position(minLng, minLat)),
        Point(coordinates: Position(maxLng, maxLat)),
      ],
      CameraOptions(),
      MbxEdgeInsets(top: 100, left: 60, bottom: 340, right: 60),
      null,
      null,
    ).then((cam) async {
      final zoom = (cam.zoom ?? 10).clamp(3.0, 13.0);
      await ctrl.flyTo(
        CameraOptions(center: cam.center, zoom: zoom, padding: cam.padding),
        MapAnimationOptions(duration: 1100),
      );
    }).catchError((_) async {
      final midLat = (minLat + maxLat) / 2;
      final midLng = (minLng + maxLng) / 2;
      await ctrl.flyTo(
        CameraOptions(
          center: Point(coordinates: Position(midLng, midLat)),
          zoom: 7,
        ),
        MapAnimationOptions(duration: 900),
      );
    });
  }

  Future<void> _onSetOrigin(SetOrigin e, Emitter<RouteBuilderState> emit) async {
    emit(state.copyWith(origin: e.location, clearError: true));
    await _fitCamera([
      e.location,
      if (state.destination != null) state.destination!,
    ]);
  }

  Future<void> _onSetDestination(SetDestination e, Emitter<RouteBuilderState> emit) async {
    emit(state.copyWith(
      destination: e.location,
      clearError: true,
      step: state.origin != null ? AppStep.waypoints : state.step,
    ));
    await _fitCamera([
      if (state.origin != null) state.origin!,
      e.location,
    ]);
  }

  void _onSetSport(SetSport e, Emitter<RouteBuilderState> emit) {
    emit(state.copyWith(sport: e.sport, speedKmh: e.sport.defaultSpeedKmh));
    _prefs.setString(_kSportKey, e.sport.name);
  }

  void _onSetUnit(SetUnit e, Emitter<RouteBuilderState> emit) {
    emit(state.copyWith(unit: e.unit));
    _prefs.setString(_kUnitKey, e.unit.name);
  }

  Future<void> _onAddVia(AddViaPoint e, Emitter<RouteBuilderState> emit) async {
    final updated = [...state.viaPoints, e.location];
    emit(state.copyWith(viaPoints: updated));
    await _fitCamera([
      if (state.origin != null) state.origin!,
      ...updated,
      if (state.destination != null) state.destination!,
    ]);
  }

  void _onRemoveVia(RemoveViaPoint e, Emitter<RouteBuilderState> emit) {
    final updated = [...state.viaPoints]..removeAt(e.index);
    emit(state.copyWith(viaPoints: updated));
  }

  Future<void> _onRequestSuggestions(
    RequestSuggestions e,
    Emitter<RouteBuilderState> emit,
  ) async {
    if (state.origin == null || state.destination == null) return;
    emit(state.copyWith(loading: true, clearError: true));

    final result = await _suggestWaypoints(SuggestWaypointsParams(
      origin: state.origin!,
      destination: state.destination!,
      sport: state.sport,
    ));

    result.fold(
      (f) => emit(state.copyWith(loading: false, error: f.message)),
      (waypoints) => emit(state.copyWith(
        loading: false,
        suggestions: waypoints,
        usingSuggestions: true,
        step: AppStep.waypoints,
      )),
    );
  }

  void _onAcceptSuggestions(AcceptSuggestions e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(usingSuggestions: true));

  Future<void> _onGenerateRoute(
    GenerateRoute e,
    Emitter<RouteBuilderState> emit,
  ) async {
    if (state.origin == null || state.destination == null) return;
    emit(state.copyWith(loading: true, step: AppStep.generating, clearError: true));

    final via = state.usingSuggestions
        ? state.suggestions.map((w) => w.location).toList()
        : state.viaPoints;

    final result = await _buildRoute(BuildRouteParams(
      origin: state.origin!,
      destination: state.destination!,
      viaPoints: via,
      sport: state.sport,
      unit: state.unit,
    ));

    result.fold(
      (f) => emit(state.copyWith(
        loading: false,
        step: AppStep.waypoints,
        error: f.message,
      )),
      (route) async {
        await _local.saveRoute(route);

        emit(state.copyWith(
          loading: false,
          route: route,
          step: AppStep.map,
        ));
        await _fitCamera([route.origin, route.destination]);
      },
    );
  }

  void _onSetDays(SetDays e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(days: e.days));

  void _onSetSpeed(SetSpeed e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(speedKmh: e.kmh));

  void _onSetStartTime(SetStartTime e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(startTime: e.time));

  void _onToggleBreak(ToggleBreak e, Emitter<RouteBuilderState> emit) {
    final updated = Set<BreakType>.from(state.selectedBreaks);
    if (updated.contains(e.type)) {
      updated.remove(e.type);
    } else {
      updated.add(e.type);
    }
    emit(state.copyWith(selectedBreaks: updated));
  }

  Future<void> _onBuildPlan(
    BuildPlanEvent e,
    Emitter<RouteBuilderState> emit,
  ) async {
    if (state.route == null) return;
    emit(state.copyWith(loading: true, clearError: true));

    final result = await _buildPlan(BuildPlanParams(
      route: state.route!,
      days: state.days,
      speedKmh: state.speedKmh,
      startTime: state.startTime,
      breaks: state.selectedBreaks.toList(),
    ));

    result.fold(
      (f) => emit(state.copyWith(loading: false, error: f.message)),
      (plan) async {
        await _local.savePlanToDb(plan);

        emit(state.copyWith(
          loading: false,
          plan: plan,
          step: AppStep.review,
        ));
      },
    );
  }

  Future<void> _onExport(ExportEvent e, Emitter<RouteBuilderState> emit) async {
    if (state.plan == null) return;
    emit(state.copyWith(loading: true, clearError: true, clearExport: true));

    final result = await _exportPlan(ExportPlanParams(
      plan: state.plan!,
      format: e.format,
    ));

    result.fold(
      (f) => emit(state.copyWith(loading: false, error: f.message)),
      (path) => emit(state.copyWith(
        loading: false,
        exportedPath: path,
        step: AppStep.export,
      )),
    );
  }

  void _onGoToStep(GoToStep e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(step: e.step, clearError: true));

  void _onReset(ResetAll e, Emitter<RouteBuilderState> emit) => emit(
        RouteBuilderState(
          startTime: DateTime.now().copyWith(hour: 7, minute: 0, second: 0, millisecond: 0),
          mapController: state.mapController,
          sport: state.sport,
          unit: state.unit,
        ),
      );
}
