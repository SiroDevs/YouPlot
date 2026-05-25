import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../bloc/plan_config/plan_config_cubit.dart';
import '../../../bloc/route_builder/route_session_cubit.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/icon_text_button.dart';
import '../../../widgets/steps/step_header.dart';
import '../../../widgets/steps/section_header.dart';
import '../widgets/breaks_section.dart';
import '../widgets/days_counter.dart';
import '../widgets/speed_slider.dart';
import '../widgets/start_date_time_picker.dart';

class PlanStep4 extends StatelessWidget {
  const PlanStep4({super.key});

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<PlanConfigCubit>();

    return BlocBuilder<PlanConfigCubit, PlanConfigState>(
      builder: (ctx, state) {
        final unit = ctx.read<RouteSessionCubit>().state.unit;
        final sport = ctx.read<RouteSessionCubit>().state.sport;
        final b = Theme.of(ctx).brightness;
        final isRunning = sport == SportType.running;

        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Column(
            children: [
              StepHeader(
                showBack: true,
                onBack: cubit.goBack,
                stepNumber: 3,
                totalSteps: 5,
              ),
              SingleChildScrollView(
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
                      onChanged: cubit.setStartTime,
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
                      onChanged: cubit.setDays,
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
                      sport: sport,
                      unit: unit,
                      brightness: b,
                      onChanged: cubit.setSpeed,
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
                      onToggle: cubit.toggleBreak,
                    ),
                    const Gap(16),

                    if (state.error != null) ...[
                      ErrorBar(message: state.error!),
                      const Gap(12),
                    ],
                    const Gap(8),
                  ],
                ),
              ).expanded(),
              IconTextButton(
                label: 'Create Plan',
                icon: Icons.check_rounded,
                brightness: b,
                loading: state.loading,
                onPressed: cubit.buildPlan,
              ),
              const Gap(12),
            ],
          ),
        );
      },
    );
  }
}
