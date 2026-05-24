import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';

part 'route_session_state.dart';

const _kSportKey = 'sport_pref_v1';
const _kUnitKey = 'unit_pref_v1';

/// Lightweight session cubit that only holds data shared between steps:
/// sport, unit, origin, destination, route, plan, and the map controller.
/// Each step cubit reads from / writes back to this session.
class RouteSessionCubit extends Cubit<RouteSessionState> {
  final SharedPreferences _prefs;

  RouteSessionCubit(this._prefs)
      : super(
          RouteSessionState(
            sport: SportType.values.firstWhere(
              (s) => s.name == _prefs.getString(_kSportKey),
              orElse: () => SportType.cycling,
            ),
            unit: DistanceUnit.values.firstWhere(
              (u) => u.name == _prefs.getString(_kUnitKey),
              orElse: () => DistanceUnit.kilometers,
            ),
          ),
        );

  // ── Navigation ─────────────────────────────────────────────────────────────

  void goToStep(AppStep step) => emit(state.copyWith(step: step));

  // ── Setup data ─────────────────────────────────────────────────────────────

  void setSport(SportType sport) {
    emit(state.copyWith(sport: sport));
    _prefs.setString(_kSportKey, sport.name);
  }

  void setUnit(DistanceUnit unit) {
    emit(state.copyWith(unit: unit));
    _prefs.setString(_kUnitKey, unit.name);
  }

  void setOrigin(Location loc) => emit(state.copyWith(origin: loc));

  void setDestination(Location loc) => emit(state.copyWith(destination: loc));

  // ── Route & plan (written by step cubits) ──────────────────────────────────

  void setRoute(RouteMap route) =>
      emit(state.copyWith(route: route, step: AppStep.map));

  void setPlan(RoutePlan plan) =>
      emit(state.copyWith(plan: plan, step: AppStep.review));

  // ── Map controller ─────────────────────────────────────────────────────────

  void setMapController(MapController ctrl) =>
      emit(state.copyWith(mapController: ctrl));

  // ── Import shortcut ────────────────────────────────────────────────────────

  void setImportedRoute(RouteMap route) {
    emit(state.copyWith(
      route: route,
      sport: route.sport,
      step: AppStep.plan,
    ));
  }

  // ── Reset ──────────────────────────────────────────────────────────────────

  void reset() => emit(RouteSessionState(
        mapController: state.mapController,
        sport: state.sport,
        unit: state.unit,
      ));
}
