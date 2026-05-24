import 'dart:math';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../route_builder/route_session_cubit.dart';

part 'setup_state.dart';

/// Cubit for Step 1: sport selector, unit toggle, origin & destination pickers.
/// Writes back to [RouteSessionCubit] when the user taps "Continue".
class SetupCubit extends Cubit<SetupState> {
  final RouteSessionCubit _session;

  SetupCubit(this._session)
      : super(SetupState(
          sport: _session.state.sport,
          unit: _session.state.unit,
          origin: _session.state.origin,
          destination: _session.state.destination,
        ));

  // ── Local mutations ────────────────────────────────────────────────────────

  void setSport(SportType sport) {
    emit(state.copyWith(sport: sport));
    _session.setSport(sport);
  }

  void setUnit(DistanceUnit unit) {
    emit(state.copyWith(unit: unit));
    _session.setUnit(unit);
  }

  void setOrigin(Location loc) {
    emit(state.copyWith(origin: loc, clearError: true));
    _session.setOrigin(loc);
    _fitCamera();
  }

  void setDestination(Location loc) {
    emit(state.copyWith(destination: loc, clearError: true));
    _session.setDestination(loc);
    _fitCamera();
  }

  void dismissError() => emit(state.copyWith(clearError: true));

  // ── Navigation ─────────────────────────────────────────────────────────────

  void proceed() {
    if (!state.canProceed) return;
    _session.goToStep(AppStep.waypoints);
  }

  // ── Map helper ────────────────────────────────────────────────────────────

  void _fitCamera() {
    final ctrl = _session.state.mapController;
    final locs = [
      if (state.origin != null) state.origin!,
      if (state.destination != null) state.destination!,
    ];
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
