import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/setup/setup_cubit.dart';
import '../../../widgets/maps/map_background.dart';
import '../../../widgets/maps/map_search_field.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/icon_text_button.dart';
import '../../../widgets/steps/step_header.dart';
import '../../../widgets/steps/badges.dart';
import '../../../widgets/steps/general.dart';
import '../../../widgets/steps/sport_dropdown.dart';

class PlanStep1 extends StatelessWidget {
  const PlanStep1({super.key});

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<SetupCubit>();
    return BlocBuilder<SetupCubit, SetupState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;
        return Stack(
          children: [
            MapBackground(),
            Column(
              children: [
                const StepHeader(
                  showBack: true,
                  showThemeToggle: true,
                  stepNumber: 1,
                  totalSteps: 5,
                ),
                const Spacer(),
                Container(
                  margin: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.black.withValues(alpha: 0.88)
                        : Colors.white.withValues(alpha: 0.96),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Plot your route',
                          style: Theme.of(ctx).textTheme.displayMedium,
                        ).animate().fadeIn(delay: 50.ms),
                        const Gap(10),
                        SportDropdown(
                          brightness: b,
                          value: state.sport,
                          onChanged: (s) {
                            if (s != null) cubit.setSport(s);
                          },
                        ).animate().fadeIn(delay: 120.ms),
                        const Gap(10),

                        FieldLabel(
                          label: 'Starting point',
                          icon: Icons.trip_origin_rounded,
                          brightness: b,
                        ),
                        const Gap(8),
                        MapSearchField(
                          hint: 'Search city or address…',
                          value: state.origin,
                          showGps: true,
                          onSelected: cubit.setOrigin,
                          brightness: b,
                        ).animate().fadeIn(delay: 160.ms),
                        if (state.origin != null) ...[
                          const Gap(4),
                          SelectedBadge(
                            label: state.origin!.address ??
                                state.origin!.name ??
                                '',
                            brightness: b,
                          ),
                        ],
                        const Gap(16),

                        FieldLabel(
                          label: 'Destination',
                          icon: Icons.flag_rounded,
                          brightness: b,
                        ),
                        const Gap(8),
                        MapSearchField(
                          hint: 'Search destination…',
                          value: state.destination,
                          onSelected: cubit.setDestination,
                          brightness: b,
                        ).animate().fadeIn(delay: 200.ms),
                        if (state.destination != null) ...[
                          const Gap(4),
                          SelectedBadge(
                            label: state.destination!.address ??
                                state.destination!.name ??
                                '',
                            brightness: b,
                          ),
                        ],
                        const Gap(20),

                        if (state.error != null) ...[
                          ErrorBar(
                            message: state.error!,
                            onDismiss: cubit.dismissError,
                          ),
                          const Gap(12),
                        ],

                        IconTextButton(
                          label: 'Continue',
                          icon: Icons.arrow_forward_rounded,
                          brightness: b,
                          onPressed:
                              state.canProceed ? cubit.proceed : null,
                        ).animate().fadeIn(delay: 240.ms),
                        const Gap(8),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ],
        );
      },
    );
  }
}
