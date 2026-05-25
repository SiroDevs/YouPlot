import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../bloc/route_builder/route_session_cubit.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/maps/map_convas.dart';
import '../../../widgets/steps/step_header.dart';
import '../widgets/route_panel.dart';

class PlanStep3 extends StatelessWidget {
  const PlanStep3({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RouteSessionCubit, RouteSessionState>(
      builder: (ctx, session) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;

        if (session.route == null) {
          return _GeneratingView(brightness: b, isDark: isDark);
        }

        final route = session.route!;
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
                  onBack: () => ctx.read<RouteSessionCubit>().goToStep(AppStep.waypoints),
                  onReset: () => ctx.read<RouteSessionCubit>().reset(),
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
                  onPlan: () =>
                      ctx.read<RouteSessionCubit>().goToStep(AppStep.plan),
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
        Positioned.fill(
          child: Container(
            color: isDark ? const Color(0xFF0D1B2A) : const Color(0xFFE8E0D8),
          ),
        ),
        Positioned(
          top: 0, left: 0, right: 0,
          child: StepHeader(showBack: false, stepNumber: 2, totalSteps: 5),
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
