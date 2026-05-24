import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../core/di/injection.dart';
import '../../bloc/route_builder/route_session_cubit.dart';
import '../../bloc/setup/setup_cubit.dart';
import '../../bloc/waypoints/waypoints_cubit.dart';
import '../../bloc/plan_config/plan_config_cubit.dart';
import '../../bloc/review/review_cubit.dart';
import '../../theme/app_colors.dart';
import '../../widgets/state_widgets.dart';
import 'steps/plan_step1.dart';
import 'steps/plan_step2.dart';
import 'steps/plan_step3.dart';
import 'steps/plan_step4.dart';
import 'steps/plan_step5.dart';

/// Provides [RouteSessionCubit] and all step-level cubits, then delegates
/// rendering to the correct step widget based on [RouteSessionState.step].
class PlanMakerScreen extends StatelessWidget {
  const PlanMakerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // RouteSessionCubit is the single source of truth for cross-step data.
    // Each step cubit is created once and kept alive for the session lifetime
    // so state is not lost when stepping back and forth.
    return BlocProvider<RouteSessionCubit>(
      create: (_) => sl<RouteSessionCubit>(),
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
            // MapRoute step has no cubit — it reads from session directly.
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
      // Only rebuild when the step changes — avoids spurious rebuilds from
      // origin/destination updates deep inside step widgets.
      buildWhen: (prev, next) => prev.step != next.step,
      builder: (ctx, session) {
        final b = Theme.of(ctx).brightness;
        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Stack(
            children: [
              AnimatedSwitcher(
                duration: const Duration(milliseconds: 280),
                transitionBuilder: (child, anim) =>
                    FadeTransition(opacity: anim, child: child),
                child: KeyedSubtree(
                  key: ValueKey(session.step),
                  child: _pageForStep(session.step),
                ),
              ),
              // Global loading overlay for steps that do async work and
              // want to block the whole screen (waypoints suggestion fetch).
              // Steps manage their own in-widget loaders otherwise.
            ],
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
