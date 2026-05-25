import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../core/constants/app_constants.dart';
import '../../../core/di/injection.dart';
import '../../../domain/entities/route_map.dart';
import '../../bloc/route_builder/route_session_cubit.dart';
import '../../bloc/setup/setup_cubit.dart';
import '../../bloc/waypoints/waypoints_cubit.dart';
import '../../bloc/plan_config/plan_config_cubit.dart';
import '../../bloc/review/review_cubit.dart';
import '../../theme/app_colors.dart';
import 'steps/plan_step1.dart';
import 'steps/plan_step2.dart';
import 'steps/plan_step3.dart';
import 'steps/plan_step4.dart';
import 'steps/plan_step5.dart';

/// Provides [RouteSessionCubit] and all step-level cubits, then delegates
/// rendering to the correct step widget based on [RouteSessionState.step].
///
/// Pass [importedRoute] when opening from RouteDetailScreen or PlanDetailScreen
/// to skip setup/waypoints and land directly on the plan-config step.
class PlanMakerScreen extends StatelessWidget {
  final RouteMap? importedRoute;

  const PlanMakerScreen({super.key, this.importedRoute});

  @override
  Widget build(BuildContext context) {
    return BlocProvider<RouteSessionCubit>(
      create: (_) {
        final session = sl<RouteSessionCubit>();
        // Seed from an imported route if provided — this transitions the
        // session straight to AppStep.plan so the user skips steps 1-3.
        if (importedRoute != null) {
          session.setImportedRoute(importedRoute!);
        }
        return session;
      },
      child: Builder(builder: (ctx) {
        final session = ctx.read<RouteSessionCubit>();
        return MultiBlocProvider(
          providers: [
            BlocProvider(create: (_) => SetupCubit(session)),
            BlocProvider(
              create: (_) => WaypointsCubit(
                suggestWaypoints: sl(),
                buildRoute: sl(),
                local: sl(),
                session: session,
              ),
            ),
            // Step 3 (map) has no cubit — reads session directly.
            BlocProvider(
              create: (_) => PlanConfigCubit(
                buildPlan: sl(),
                local: sl(),
                session: session,
              ),
            ),
            BlocProvider(
              create: (_) => ReviewCubit(exportPlan: sl(), session: session),
            ),
          ],
          child: const _PlanMakerBody(),
        );
      }),
    );
  }
}

class _PlanMakerBody extends StatelessWidget {
  const _PlanMakerBody();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RouteSessionCubit, RouteSessionState>(
      buildWhen: (prev, next) => prev.step != next.step,
      builder: (ctx, session) {
        final b = Theme.of(ctx).brightness;
        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: AnimatedSwitcher(
            duration: const Duration(milliseconds: 280),
            transitionBuilder: (child, anim) =>
                FadeTransition(opacity: anim, child: child),
            child: KeyedSubtree(
              key: ValueKey(session.step),
              child: _pageForStep(session.step),
            ),
          ),
        );
      },
    );
  }

  Widget _pageForStep(AppStep step) {
    switch (step) {
      case AppStep.setup:
        return const PlanStep1();
      case AppStep.waypoints:
        return const PlanStep2();
      case AppStep.map:
        return const PlanStep3();
      case AppStep.plan:
        return const PlanStep4();
      case AppStep.review:
        return const PlanStep5();
    }
  }
}
