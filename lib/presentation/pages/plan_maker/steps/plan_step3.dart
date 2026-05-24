import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/maps/map_convas.dart';
import '../../../widgets/steps/step_header.dart';
import '../widgets/route_panel.dart';

class PlanStep3 extends StatelessWidget {
  const PlanStep3({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;

        if (state.step == AppStep.map || state.route == null) {
          return _GeneratingView(brightness: b, isDark: isDark);
        }

        final route = state.route!;
        return Scaffold(
          backgroundColor:
              isDark ? const Color(0xFF0D1B2A) : const Color(0xFFE8E0D8),
          body: Stack(
            children: [
              Positioned.fill(
                child: MapCanvas(
                  route: route,
                  brightness: b,
                  isDark: isDark,
                  stepNumber: 2,
                  totalSteps: 5,
                  onBack: () => bloc.add(GoToStep(AppStep.waypoints)),
                  onReset: () => bloc.add(ResetAll()),
                ),
              ),
              Positioned(
                left: 0,
                right: 0,
                bottom: 0,
                child: RoutePanel(
                  route: route,
                  brightness: b,
                  isDark: isDark,
                  onPlan: () => bloc.add(GoToStep(AppStep.plan)),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _GeneratingView extends StatelessWidget {
  final Brightness brightness;
  final bool isDark;

  const _GeneratingView({required this.brightness, required this.isDark});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Stack(
      children: [
        // Blurred map bg
        const Positioned.fill(child: _MapPlaceholder()),
        // Header
        Positioned(
          top: 0, left: 0, right: 0,
          child: StepHeader(
            showBack: false,
            stepNumber: 2,
            totalSteps: 5,
          ),
        ),
        Center(
          child: Container(
            padding: const EdgeInsets.all(32),
            margin: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: isDark
                  ? Colors.black.withValues(alpha: 0.88)
                  : Colors.white.withValues(alpha: 0.97),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.15),
                  blurRadius: 24,
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: AppColors.primaryDim,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Center(
                    child: CircularProgressIndicator(
                      color: AppColors.primary,
                      strokeWidth: 2.5,
                    ),
                  ),
                ),
                const Gap(20),
                Text(
                  'Building your route',
                  style: TextStyle(
                    color: AppColors.textPrimary(b),
                    fontSize: 17,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const Gap(6),
                Text(
                  'Fetching directions & elevation data…',
                  style: TextStyle(
                    color: AppColors.textSecondary(b),
                    fontSize: 13,
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _MapPlaceholder extends StatelessWidget {
  const _MapPlaceholder();

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Container(
      color: b == Brightness.dark
          ? const Color(0xFF0D1B2A)
          : const Color(0xFFE8E0D8),
    );
  }
}
