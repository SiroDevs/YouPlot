import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/step_bottom_button.dart';
import '../../../widgets/steps/step_header.dart';
import '../../../widgets/steps/section_header.dart';
import '../widgets/step5_components.dart';

class PlanStep5 extends StatelessWidget {
  const PlanStep5({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isRunning = state.sport == SportType.running;
        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Column(
            children: [
              StepHeader(
                showBack: true,
                onBack: () => bloc.add(GoToStep(AppStep.map)),
                stepNumber: 3,
                totalSteps: 5,
              ),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Gap(16),
                      Text(
                        'Plan your activity',
                        style: Theme.of(ctx).textTheme.displayMedium,
                      ),
                      const Gap(4),
                      Text(
                        'Set your schedule, pace and preferences.',
                        style: TextStyle(
                          fontSize: 13,
                          color: AppColors.textSecondary(b),
                        ),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.calendar_today_rounded,
                        label: 'Start date & time',
                        brightness: b,
                      ),
                      const Gap(12),
                      StartDateTimePicker(
                        startTime: state.startTime,
                        brightness: b,
                        onChanged: (dt) => bloc.add(SetStartTime(dt)),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.calendar_month_rounded,
                        label: 'Total days',
                        brightness: b,
                      ),
                      const Gap(12),
                      DaysCounter(
                        days: state.days,
                        brightness: b,
                        onChanged: (d) => bloc.add(SetDays(d)),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.speed_rounded,
                        label: isRunning ? 'Pace' : 'Speed',
                        brightness: b,
                      ),
                      const Gap(12),
                      SpeedSlider(
                        speed: state.speed,
                        sport: state.sport,
                        unit: state.unit,
                        brightness: b,
                        onChanged: (v) => bloc.add(SetSpeed(v)),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.coffee_rounded,
                        label: 'Activity breaks',
                        brightness: b,
                      ),
                      const Gap(4),
                      BreaksSection(
                        selected: state.selectedBreaks,
                        brightness: b,
                        onToggle: (t) => bloc.add(ToggleBreak(t)),
                      ),
                      const Gap(16),

                      if (state.error != null) ...[
                        ErrorBar(message: state.error!),
                        const Gap(12),
                      ],
                      const Gap(8),
                    ],
                  ),
                ),
              ),
              StepBottomButton(
                label: 'Create Plan',
                icon: Icons.check_rounded,
                brightness: b,
                loading: state.loading,
                onPressed: () => bloc.add(BuildPlanEvent()),
              ),
              const Gap(12),
            ],
          ),
        );
      },
    );
  }
}
