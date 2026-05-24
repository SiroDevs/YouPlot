import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/usecases/build_plan.dart';
import '../../../domain/repositories/local_repository.dart';
import '../route_builder/route_session_cubit.dart';

part 'plan_config_state.dart';

/// Cubit for Step 4: days, speed, start time, break toggles → build plan.
class PlanConfigCubit extends Cubit<PlanConfigState> {
  final BuildPlan _buildPlan;
  final LocalRepository _local;
  final RouteSessionCubit _session;

  PlanConfigCubit({
    required BuildPlan buildPlan,
    required LocalRepository local,
    required RouteSessionCubit session,
  })  : _buildPlan = buildPlan,
        _local = local,
        _session = session,
        super(PlanConfigState(
          startTime: DateTime.now().copyWith(
            hour: 7, minute: 0, second: 0, millisecond: 0,
          ),
          speed: session.state.sport.defaultSpeedKmh,
        ));

  // ── Field updates ──────────────────────────────────────────────────────────

  void setDays(int days) => emit(state.copyWith(days: days));

  void setSpeed(double kmh) => emit(state.copyWith(speed: kmh));

  void setStartTime(DateTime time) => emit(state.copyWith(startTime: time));

  void toggleBreak(BreakType type) {
    final updated = Set<BreakType>.from(state.selectedBreaks);
    if (updated.contains(type)) {
      updated.remove(type);
    } else {
      updated.add(type);
    }
    emit(state.copyWith(selectedBreaks: updated));
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  void goBack() => _session.goToStep(AppStep.map);

  // ── Plan build ─────────────────────────────────────────────────────────────

  Future<void> buildPlan() async {
    final route = _session.state.route;
    if (route == null) return;

    emit(state.copyWith(loading: true, clearError: true));

    final result = await _buildPlan(
      BuildPlanParams(
        route: route,
        days: state.days,
        speed: state.speed,
        startTime: state.startTime,
        breaks: state.selectedBreaks.toList(),
      ),
    );

    if (result.isLeft()) {
      emit(state.copyWith(
        loading: false,
        error: result.fold((f) => f.message, (_) => ''),
      ));
      return;
    }

    final plan = result.getOrElse(() => throw StateError('unreachable'));
    await _local.savePlanToDb(plan);

    emit(state.copyWith(loading: false));
    _session.setPlan(plan); // transitions session to AppStep.review
  }
}
