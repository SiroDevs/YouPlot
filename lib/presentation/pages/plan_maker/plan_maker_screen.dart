import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../../widgets/state_widgets.dart';
import 'steps/plan_step1.dart';
import 'steps/plan_step2.dart';
import 'steps/plan_step3.dart';
import 'steps/plan_step4.dart';
import 'steps/plan_step5.dart';

class PlanMakerScreen extends StatelessWidget {
  const PlanMakerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
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
                  key: ValueKey(state.step),
                  child: _pageForStep(state),
                ),
              ),
              if (state.loading && state.step != AppStep.generating)
                LoadingOverlay(message: _loadingMessage(state.step)),
            ],
          ),
        );
      },
    );
  }

  Widget _pageForStep(RouteBuilderState state) {
    switch (state.step) {
      case AppStep.setup:
        return const PlanStep1();
      case AppStep.waypoints:
        return const PlanStep2();
      case AppStep.generating:
      case AppStep.map:
        return const PlanStep3();
      case AppStep.plan:
        return const PlanStep4();
      case AppStep.review:
      case AppStep.export:
        return const PlanStep5();
    }
  }

  String _loadingMessage(AppStep step) {
    switch (step) {
      case AppStep.generating:
        return 'Building route…';
      case AppStep.waypoints:
        return 'Finding waypoints…';
      case AppStep.plan:
        return 'Scheduling plan…';
      case AppStep.export:
        return 'Exporting file…';
      default:
        return 'Please wait…';
    }
  }
}
