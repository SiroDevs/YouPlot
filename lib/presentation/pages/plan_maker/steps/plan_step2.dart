import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/route_builder/route_session_cubit.dart';
import '../../../bloc/waypoints/waypoints_cubit.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/icon_text_button.dart';
import '../../../widgets/steps/step_header.dart';
import '../../../widgets/steps/general.dart';
import '../../../widgets/steps/section_header.dart';
import '../../../widgets/steps/home_rows.dart';
import '../../../widgets/maps/map_background.dart';
import '../../../widgets/steps/option_card.dart';

class PlanStep2 extends StatelessWidget {
  const PlanStep2({super.key});

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<WaypointsCubit>();

    // Read origin/destination names from session (they never change here)
    final session = context.read<RouteSessionCubit>();

    return BlocBuilder<WaypointsCubit, WaypointsState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;
        final sessionState = session.state;

        return Stack(
          children: [
            MapBackground(),
            Column(
              children: [
                StepHeader(
                  showBack: true,
                  onBack: cubit.goBack,
                  showNew: true,
                  onNew: () => session.reset(),
                  stepNumber: 2,
                  totalSteps: 5,
                ),
                const Spacer(),
                Container(
                  margin: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.black.withValues(alpha: 0.88)
                        : Colors.white.withValues(alpha: 0.96),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Route waypoints',
                          style: Theme.of(ctx).textTheme.displayMedium,
                        ),
                        const Gap(4),
                        Text(
                          '${sessionState.origin?.name ?? "Start"} → ${sessionState.destination?.name ?? "End"}',
                          style: TextStyle(
                            color: AppColors.textSecondary(b),
                            fontSize: 13,
                          ),
                        ),
                        const Gap(20),

                        OptionCard(
                          key: const ValueKey('app_suggestions'),
                          brightness: b,
                          emoji: '🤖',
                          title: 'App suggestions',
                          subtitle: 'Auto-pick major towns along the way',
                          onTap: cubit.requestSuggestions,
                        ),
                        const Gap(10),
                        OptionCard(
                          key: const ValueKey('my_own_stops'),
                          brightness: b,
                          emoji: '✏️',
                          title: 'My own stops',
                          subtitle:
                              'Add specific places you want to pass through',
                          onTap: () => _showCustomSheet(ctx, cubit),
                        ),
                        const Gap(10),
                        OptionCard(
                          key: const ValueKey('direct_route'),
                          brightness: b,
                          emoji: '⚡',
                          title: 'Direct route',
                          subtitle:
                              'No intermediate stops — straight from A to B',
                          onTap: cubit.generateRoute,
                        ),

                        if (state.viaPoints.isNotEmpty) ...[
                          const Gap(16),
                          SectionHeader(
                            key: const ValueKey('header_stops'),
                            icon: Icons.place_rounded,
                            label: 'Your stops',
                            brightness: b,
                          ),
                          const Gap(8),
                          ...state.viaPoints.asMap().entries.map(
                            (e) => Padding(
                              padding: const EdgeInsets.only(bottom: 6),
                              child: ViaRow(
                                index: e.key,
                                location: e.value,
                                onRemove: () => cubit.removeVia(e.key),
                                brightness: b,
                              ),
                            ),
                          ),
                          const Gap(12),
                          IconTextButton(
                            label: 'Generate Route',
                            icon: Icons.map_rounded,
                            brightness: b,
                            onPressed: cubit.generateRoute,
                          ),
                        ],

                        if (state.suggestions.isNotEmpty) ...[
                          const Gap(16),
                          SectionHeader(
                            key: const ValueKey('suggested_stops'),
                            icon: Icons.auto_awesome_rounded,
                            label: 'Suggested stops',
                            brightness: b,
                          ),
                          const Gap(8),
                          ...state.suggestions.map(
                            (w) => WaypointRow(waypoint: w, brightness: b),
                          ),
                          const Gap(12),
                          IconTextButton(
                            label: 'Generate Route with These Stops',
                            icon: Icons.map_rounded,
                            brightness: b,
                            onPressed: cubit.generateRoute,
                          ),
                        ],

                        if (state.loading) ...[
                          const Gap(12),
                          const Center(child: CircularProgressIndicator()),
                        ],

                        if (state.error != null) ...[
                          const Gap(12),
                          ErrorBar(message: state.error!),
                        ],
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

  void _showCustomSheet(BuildContext ctx, WaypointsCubit cubit) {
    showModalBottomSheet(
      context: ctx,
      isScrollControlled: true,
      useRootNavigator: true,
      builder: (_) =>
          BlocProvider.value(value: cubit, child: const AddViaSheet()),
    );
  }
}
