import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../../widgets/state_widgets.dart';
import 'steps/generating_step.dart';
import 'steps/map_step.dart';
import 'steps/plan_step.dart';
import 'steps/review_step.dart';
import 'steps/setup_step.dart';
import 'steps/export_step.dart';
import 'steps/waypoint_step.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

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
              if (state.loading)
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
        return const SetupStep();
      case AppStep.waypoints:
        return const WaypointsStep();
      case AppStep.generating:
        return const GeneratingStep();
      case AppStep.map:
        return const MapStep();
      case AppStep.plan:
        return const PlanStep();
      case AppStep.review:
        return const ReviewStep();
      case AppStep.export:
        return const ExportStep();
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
