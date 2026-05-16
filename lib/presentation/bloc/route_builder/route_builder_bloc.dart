import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../../../../domain/entities/entities.dart';
import '../../../../../../domain/usecases/usecases.dart';

part 'route_builder_event.dart';
part 'route_builder_state.dart';

class RouteBuilderBloc extends Bloc<RouteBuilderEvent, RouteBuilderState> {
  final BuildRoute _buildRoute;
  final SuggestWaypoints _suggestWaypoints;
  final BuildPlan _buildPlan;
  final ExportPlan _exportPlan;

  RouteBuilderBloc({
    required BuildRoute buildRoute,
    required SuggestWaypoints suggestWaypoints,
    required BuildPlan buildPlan,
    required ExportPlan exportPlan,
  })  : _buildRoute = buildRoute,
        _suggestWaypoints = suggestWaypoints,
        _buildPlan = buildPlan,
        _exportPlan = exportPlan,
        super(RouteBuilderState(
          startTime: DateTime.now().copyWith(hour: 7, minute: 0, second: 0, millisecond: 0),
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
  }

  void _onSetOrigin(SetOrigin e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(origin: e.location, clearError: true));

  void _onSetDestination(SetDestination e, Emitter<RouteBuilderState> emit) {
    emit(state.copyWith(
      destination: e.location,
      clearError: true,
      step: state.origin != null ? AppStep.waypoints : state.step,
    ));
  }

  void _onSetSport(SetSport e, Emitter<RouteBuilderState> emit) {
    emit(state.copyWith(sport: e.sport, speedKmh: e.sport.defaultSpeedKmh));
  }

  void _onSetUnit(SetUnit e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(unit: e.unit));

  void _onAddVia(AddViaPoint e, Emitter<RouteBuilderState> emit) =>
      emit(state.copyWith(viaPoints: [...state.viaPoints, e.location]));

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
      (route) => emit(state.copyWith(
        loading: false,
        route: route,
        step: AppStep.map,
      )),
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
      (plan) => emit(state.copyWith(
        loading: false,
        plan: plan,
        step: AppStep.review,
      )),
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
        ),
      );
}
