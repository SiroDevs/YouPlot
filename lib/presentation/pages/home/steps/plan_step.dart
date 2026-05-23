import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../../../../core/utils/formatters.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/break_selector.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/buttons.dart';
import '../../../widgets/steps/headers.dart';

class PlanStep extends StatelessWidget {
  const PlanStep({super.key});

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
              AppHeader(
                showBack: true,
                onBack: () => bloc.add(GoToStep(AppStep.map)),
              ),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Plan your journey',
                        style: Theme.of(ctx).textTheme.displayMedium,
                      ),
                      const Gap(4),
                      Text(
                        'Days, speed, start time and breaks.',
                        style: TextStyle(
                          color: AppColors.textSecondary(b),
                          fontSize: 13,
                        ),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.calendar_today_rounded,
                        label: 'Total days',
                        brightness: b,
                      ),
                      const Gap(12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          CounterBtn(
                            icon: Icons.remove_rounded,
                            brightness: b,
                            onTap: state.days > 1
                                ? () => bloc.add(SetDays(state.days - 1))
                                : null,
                          ),
                          const Gap(24),
                          Column(
                            children: [
                              Text(
                                '${state.days}',
                                style: Theme.of(ctx).textTheme.displayLarge,
                              ),
                              Text(
                                state.days == 1 ? 'day' : 'days',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: AppColors.textSecondary(b),
                                ),
                              ),
                            ],
                          ),
                          const Gap(24),
                          CounterBtn(
                            icon: Icons.add_rounded,
                            brightness: b,
                            onTap: () => bloc.add(SetDays(state.days + 1)),
                          ),
                        ],
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.speed_rounded,
                        label: isRunning ? 'Pace' : 'Speed',
                        brightness: b,
                      ),
                      const Gap(12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            isRunning
                                ? Fmt.pace(state.speedKmh, state.unit)
                                : Fmt.speed(state.speedKmh, state.unit),
                            style: TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.w700,
                              color: AppColors.textPrimary(b),
                            ),
                          ),
                          if (isRunning)
                            Text(
                              Fmt.speed(state.speedKmh, state.unit),
                              style: TextStyle(
                                fontSize: 13,
                                color: AppColors.textSecondary(b),
                              ),
                            ),
                        ],
                      ),
                      Slider(
                        min: Fmt.minSpeed(state.sport),
                        max: Fmt.maxSpeed(state.sport),
                        value: state.speedKmh.clamp(
                          Fmt.minSpeed(state.sport),
                          Fmt.maxSpeed(state.sport),
                        ),
                        divisions:
                            ((Fmt.maxSpeed(state.sport) -
                                        Fmt.minSpeed(state.sport)) *
                                    2)
                                .round(),
                        onChanged: (v) => bloc.add(SetSpeed(v)),
                      ),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            Fmt.speedLabel(state.sport, 'slow'),
                            style: TextStyle(
                              fontSize: 10,
                              color: AppColors.textMuted(b),
                            ),
                          ),
                          Text(
                            Fmt.speedLabel(state.sport, 'fast'),
                            style: TextStyle(
                              fontSize: 10,
                              color: AppColors.textMuted(b),
                            ),
                          ),
                        ],
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.access_time_rounded,
                        label: 'Daily start time',
                        brightness: b,
                      ),
                      const Gap(12),
                      GestureDetector(
                        onTap: () async {
                          final t = await showTimePicker(
                            context: ctx,
                            initialTime: TimeOfDay.fromDateTime(
                              state.startTime,
                            ),
                          );
                          if (t != null) bloc.add(SetStartTime(t as DateTime));
                        },
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 14,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.surface(b),
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(
                              color: AppColors.border(b),
                              width: 0.5,
                            ),
                          ),
                          child: Row(
                            children: [
                              Icon(
                                Icons.access_time_rounded,
                                size: 16,
                                color: AppColors.textSecondary(b),
                              ),
                              const Gap(10),
                              Text(
                                Fmt.hhmm(state.startTime),
                                style: TextStyle(
                                  fontSize: 15,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.textPrimary(b),
                                ),
                              ),
                              const Spacer(),
                              Icon(
                                Icons.chevron_right_rounded,
                                size: 18,
                                color: AppColors.textMuted(b),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const Gap(28),

                      SectionHeader(
                        icon: Icons.coffee_rounded,
                        label: 'Activity breaks',
                        brightness: b,
                      ),
                      const Gap(4),
                      Text(
                        'Tap to include in your daily schedule',
                        style: TextStyle(
                          fontSize: 11,
                          color: AppColors.textSecondary(b),
                        ),
                      ),
                      const Gap(12),
                      BreakSelector(
                        selected: state.selectedBreaks,
                        onToggle: (t) => bloc.add(ToggleBreak(t)),
                      ),
                      const Gap(32),

                      if (state.error != null) ...[
                        ErrorBar(message: state.error!),
                        const Gap(12),
                      ],

                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: state.loading
                              ? null
                              : () => bloc.add(BuildPlanEvent()),
                          icon: const Icon(Icons.check_rounded, size: 16),
                          label: const Text('Create Plan'),
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.all(15),
                          ),
                        ),
                      ),
                      const Gap(24),
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
