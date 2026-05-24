import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';
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
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;
        return Stack(
          children: [
            MapBackground(),
            Column(
              children: [
                StepHeader(
                  showBack: true,
                  onBack: () => bloc.add(GoToStep(AppStep.setup)),
                  showNew: true,
                  onNew: () => bloc.add(ResetAll()),
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
                          '${state.origin?.name ?? "Start"} → ${state.destination?.name ?? "End"}',
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
                          onTap: () => bloc.add(RequestSuggestions()),
                        ),
                        const Gap(10),
                        OptionCard(
                            key: const ValueKey('my_own_stops'),
                          brightness: b,
                          emoji: '✏️',
                          title: 'My own stops',
                          subtitle:
                              'Add specific places you want to pass through',
                          onTap: () => _showCustomSheet(ctx, bloc),
                        ),
                        const Gap(10),
                        OptionCard(
                            key: const ValueKey('direct_route'),
                          brightness: b,
                          emoji: '⚡',
                          title: 'Direct route',
                          subtitle:
                              'No intermediate stops — straight from A to B',
                          onTap: () => bloc.add(GenerateRoute()),
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
                                onRemove: () => bloc.add(RemoveViaPoint(e.key)),
                                brightness: b,
                              ),
                            ),
                          ),
                          const Gap(12),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton.icon(
                              onPressed: () => bloc.add(GenerateRoute()),
                              icon: const Icon(Icons.map_rounded, size: 16),
                              label: const Text('Generate Route'),
                            ),
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
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton.icon(
                              onPressed: () => bloc.add(GenerateRoute()),
                              icon: const Icon(Icons.map_rounded, size: 16),
                              label: const Text(
                                'Generate Route with These Stops',
                              ),
                            ),
                          ),
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

  void _showCustomSheet(BuildContext ctx, RouteBuilderBloc bloc) {
    showModalBottomSheet(
      context: ctx,
      isScrollControlled: true,
      useRootNavigator: true,
      builder: (_) =>
          BlocProvider.value(value: bloc, child: const AddViaSheet()),
    );
  }
}
