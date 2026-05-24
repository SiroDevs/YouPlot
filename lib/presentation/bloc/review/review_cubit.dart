import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/usecases/export_plan.dart';
import '../route_builder/route_session_cubit.dart';

part 'review_state.dart';

/// Cubit for Step 5: view daily plan, export in various formats.
class ReviewCubit extends Cubit<ReviewState> {
  final ExportPlan _exportPlan;
  final RouteSessionCubit _session;

  ReviewCubit({
    required ExportPlan exportPlan,
    required RouteSessionCubit session,
  })  : _exportPlan = exportPlan,
        _session = session,
        super(const ReviewState());

  // ── Navigation ─────────────────────────────────────────────────────────────

  void goBack() => _session.goToStep(AppStep.plan);

  // ── Export ─────────────────────────────────────────────────────────────────

  Future<void> export(ExportFormat format) async {
    final plan = _session.state.plan;
    if (plan == null) return;

    emit(state.copyWith(loading: true, clearError: true, clearExport: true));

    final result = await _exportPlan(ExportPlanParams(plan: plan, format: format));

    if (result.isLeft()) {
      emit(state.copyWith(
        loading: false,
        error: result.fold((f) => f.message, (_) => ''),
      ));
      return;
    }

    final path = result.getOrElse(() => throw StateError('unreachable'));
    emit(state.copyWith(loading: false, exportedPath: path));
  }

  void dismissExport() => emit(state.copyWith(clearExport: true));

  void dismissError() => emit(state.copyWith(clearError: true));
}
