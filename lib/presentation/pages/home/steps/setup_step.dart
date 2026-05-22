import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/tf_widgets.dart';
import '../components/badges.dart';
import '../components/general.dart';
import '../components/headers.dart';
import '../components/map_background.dart';
import '../components/sport_dropdown.dart';

class SetupStep extends StatelessWidget {
  const SetupStep({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;
        return Stack(
          children: [
            MapBackground(brightness: b),

            Column(
              children: [
                AppHeader(),

                const Spacer(),

                Container(
                  margin: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.black.withValues(alpha: 0.88)
                        : Colors.white.withValues(alpha: 0.96),
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withValues(alpha: 0.3),
                        blurRadius: 20,
                        offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Plan your route',
                          style: Theme.of(ctx).textTheme.displayMedium,
                        ).animate().fadeIn(delay: 50.ms),
                        const Gap(4),
                        Text(
                          'Choose your sport, then pick start and end.',
                          style: TextStyle(
                            color: AppColors.textSecondary(b),
                            fontSize: 13,
                          ),
                        ).animate().fadeIn(delay: 80.ms),
                        const Gap(20),

                        // Sport dropdown
                        FieldLabel(label: 'Sport', brightness: b),
                        const Gap(8),
                        SportDropdown(
                          brightness: b,
                          value: state.sport,
                          onChanged: (s) {
                            if (s != null) bloc.add(SetSport(s));
                          },
                        ).animate().fadeIn(delay: 120.ms),
                        const Gap(20),

                        // Origin
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
                          onSelected: (loc) => bloc.add(SetOrigin(loc)),
                          brightness: b,
                        ).animate().fadeIn(delay: 160.ms),
                        if (state.origin != null) ...[
                          const Gap(4),
                          SelectedBadge(
                            label:
                                state.origin!.address ??
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
                          onSelected: (loc) => bloc.add(SetDestination(loc)),
                          brightness: b,
                        ).animate().fadeIn(delay: 200.ms),
                        if (state.destination != null) ...[
                          const Gap(4),
                          SelectedBadge(
                            label:
                                state.destination!.address ??
                                state.destination!.name ??
                                '',
                            brightness: b,
                          ),
                        ],
                        const Gap(20),

                        if (state.error != null) ...[
                          TFErrorBar(
                            message: state.error!,
                            onDismiss: () => bloc.add(ResetAll()),
                          ),
                          const Gap(12),
                        ],

                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton.icon(
                            onPressed: state.canProceed
                                ? () => bloc.add(GoToStep(AppStep.waypoints))
                                : null,
                            icon: const Icon(
                              Icons.arrow_forward_rounded,
                              size: 17,
                            ),
                            label: const Text('Continue'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.all(15),
                            ),
                          ),
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
