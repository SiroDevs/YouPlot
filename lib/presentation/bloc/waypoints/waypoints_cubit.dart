import 'dart:math';

import 'package:equatable/equatable.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../../domain/entities/waypoint.dart';
import '../../../domain/usecases/build_route.dart';
import '../../../domain/usecases/suggest_waypoints.dart';
import '../../../domain/repositories/local_repository.dart';
import '../route_builder/route_session_cubit.dart';

part 'waypoints_state.dart';

/// Cubit for Step 2: suggestion vs custom waypoints, then triggers route build.
class WaypointsCubit extends Cubit<WaypointsState> {
  final SuggestWaypoints _suggestWaypoints;
  final BuildRoute _buildRoute;
  final LocalRepository _local;
  final RouteSessionCubit _session;

  WaypointsCubit({
    required SuggestWaypoints suggestWaypoints,
    required BuildRoute buildRoute,
    required LocalRepository local,
    required RouteSessionCubit session,
  })  : _suggestWaypoints = suggestWaypoints,
        _buildRoute = buildRoute,
        _local = local,
        _session = session,
        super(const WaypointsState());

  // ── Custom via points ──────────────────────────────────────────────────────

  void addVia(Location loc) {
    final updated = [...state.viaPoints, loc];
    emit(state.copyWith(viaPoints: updated));
    _fitCamera(updated);
  }

  void removeVia(int index) {
    final updated = [...state.viaPoints]..removeAt(index);
    emit(state.copyWith(viaPoints: updated));
  }

  // ── AI suggestions ─────────────────────────────────────────────────────────

  Future<void> requestSuggestions() async {
    final origin = _session.state.origin;
    final destination = _session.state.destination;
    if (origin == null || destination == null) return;

    emit(state.copyWith(loading: true, clearError: true));

    final result = await _suggestWaypoints(
      SuggestWaypointsParams(
        origin: origin,
        destination: destination,
        sport: _session.state.sport,
      ),
    );

    result.fold(
      (f) => emit(state.copyWith(loading: false, error: f.message)),
      (waypoints) => emit(state.copyWith(
        loading: false,
        suggestions: waypoints,
        usingSuggestions: true,
      )),
    );
  }

  void acceptSuggestions() => emit(state.copyWith(usingSuggestions: true));

  // ── Route generation ───────────────────────────────────────────────────────

  Future<void> generateRoute() async {
    final origin = _session.state.origin;
    final destination = _session.state.destination;
    if (origin == null || destination == null) return;

    emit(state.copyWith(loading: true, clearError: true));
    // Immediately push session to map step so PlanStep3 shows the spinner
    _session.goToStep(AppStep.map);

    final via = state.usingSuggestions
        ? state.suggestions.map((w) => w.location).toList()
        : state.viaPoints;

    final result = await _buildRoute(
      BuildRouteParams(
        origin: origin,
        destination: destination,
        viaPoints: via,
        sport: _session.state.sport,
        unit: _session.state.unit,
      ),
    );

    if (result.isLeft()) {
      final failure = result.fold((f) => f, (_) => throw StateError('unreachable'));
      // Step back to waypoints on failure
      _session.goToStep(AppStep.waypoints);
      emit(state.copyWith(loading: false, error: failure.message));
      return;
    }

    final route = result.getOrElse(() => throw StateError('unreachable'));
    await _local.saveRoute(route);

    emit(state.copyWith(loading: false));
    _session.setRoute(route); // also transitions session to AppStep.map

    _fitCameraLocs([route.origin, route.destination]);
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  void goBack() => _session.goToStep(AppStep.setup);

  // ── Map helpers ────────────────────────────────────────────────────────────

  void _fitCamera(List<Location> via) {
    final all = [
      if (_session.state.origin != null) _session.state.origin!,
      ...via,
      if (_session.state.destination != null) _session.state.destination!,
    ];
    _fitCameraLocs(all);
  }

  void _fitCameraLocs(List<Location> locs) {
    final ctrl = _session.state.mapController;
    if (ctrl == null || locs.isEmpty) return;
    if (locs.length == 1) {
      ctrl.move(LatLng(locs[0].lat, locs[0].lng), 13.5);
      return;
    }
    double minLat = locs[0].lat, maxLat = locs[0].lat;
    double minLng = locs[0].lng, maxLng = locs[0].lng;
    for (final l in locs) {
      minLat = min(minLat, l.lat);
      maxLat = max(maxLat, l.lat);
      minLng = min(minLng, l.lng);
      maxLng = max(maxLng, l.lng);
    }
    ctrl.fitCamera(
      CameraFit.bounds(
        bounds: LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng)),
        padding: const EdgeInsets.only(
          left: 60, right: 60, top: 110, bottom: 360,
        ),
        maxZoom: 14,
      ),
    );
  }
}
