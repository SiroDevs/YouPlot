import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:share_plus/share_plus.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/utils/formatters.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/day_card.dart';
import '../../../widgets/steps/step_bottom_button.dart';
import '../../../widgets/steps/step_header.dart';

/// Combined review + export step (was steps 6 & 7).
class PlanStep6 extends StatefulWidget {
  const PlanStep6({super.key});

  @override
  State<PlanStep6> createState() => _PlanStep6State();
}

class _PlanStep6State extends State<PlanStep6>
    with SingleTickerProviderStateMixin {
  late TabController _tabs;

  @override
  void initState() {
    super.initState();
    _tabs = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabs.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final plan = state.plan!;
        final route = plan.route;
        final unit = state.unit;
        final b = Theme.of(ctx).brightness;

        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Column(
            children: [
              StepHeader(
                showBack: true,
                onBack: () => bloc.add(GoToStep(AppStep.plan)),
                stepNumber: 4,
                totalSteps: 5,
              ),

              // Stats bar
              Container(
                color: AppColors.surface(b),
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: StatCard(
                            label: 'Distance',
                            value: Fmt.distance(route.totalDistance, unit),
                          ),
                        ),
                        const Gap(8),
                        Expanded(
                          child: StatCard(
                            label: 'Total time',
                            value: Fmt.duration(plan.estimatedTotal),
                          ),
                        ),
                        const Gap(8),
                        Expanded(
                          child: StatCard(
                            label: 'Avg/day',
                            value: Fmt.distance(
                              route.totalDistance / plan.totalDays,
                              unit,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const Gap(12),
                    ElevationChart(
                      points: route.elevation,
                      unit: unit,
                      height: 80,
                    ),
                    const Gap(4),
                    TabBar(
                      controller: _tabs,
                      labelColor: AppColors.primary,
                      unselectedLabelColor: AppColors.textSecondary(b),
                      indicatorColor: AppColors.primary,
                      labelStyle: const TextStyle(
                          fontSize: 13, fontWeight: FontWeight.w600),
                      tabs: const [
                        Tab(text: 'Daily Plan'),
                        Tab(text: 'Export'),
                      ],
                    ),
                  ],
                ),
              ),

              // Tab content
              Expanded(
                child: TabBarView(
                  controller: _tabs,
                  children: [
                    // ── Tab 1: Daily segments ─────────────────────────────
                    ListView(
                      padding: const EdgeInsets.all(16),
                      children: plan.segments
                          .map(
                            (seg) => DayCard(
                              segment: seg,
                              unit: unit,
                              sport: state.sport,
                              brightness: b,
                            ),
                          )
                          .toList(),
                    ),

                    // ── Tab 2: Export ─────────────────────────────────────
                    SingleChildScrollView(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Export route',
                            style: Theme.of(ctx).textTheme.displayMedium,
                          ),
                          const Gap(4),
                          Text(
                            'Choose a format to share or use on your device.',
                            style: TextStyle(
                              color: AppColors.textSecondary(b),
                              fontSize: 13,
                            ),
                          ),
                          const Gap(20),
                          GridView.count(
                            crossAxisCount: 2,
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            crossAxisSpacing: 12,
                            mainAxisSpacing: 12,
                            childAspectRatio: 1.15,
                            children: ExportFormat.values.map((fmt) {
                              return GestureDetector(
                                onTap: state.loading
                                    ? null
                                    : () => bloc.add(ExportEvent(fmt)),
                                child: Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: AppColors.card(b),
                                    borderRadius: BorderRadius.circular(12),
                                    border: Border.all(
                                      color: AppColors.border(b),
                                      width: 0.5,
                                    ),
                                  ),
                                  child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Text(fmt.emoji,
                                          style:
                                              const TextStyle(fontSize: 30)),
                                      const Gap(8),
                                      Text(
                                        fmt.label,
                                        style: TextStyle(
                                          fontSize: 14,
                                          fontWeight: FontWeight.w600,
                                          color: AppColors.textPrimary(b),
                                        ),
                                      ),
                                      const Gap(3),
                                      Text(
                                        fmt.description,
                                        textAlign: TextAlign.center,
                                        style: TextStyle(
                                          fontSize: 10,
                                          color: AppColors.textSecondary(b),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              );
                            }).toList(),
                          ),

                          if (state.exportedPath != null) ...[
                            const Gap(20),
                            Container(
                              padding: const EdgeInsets.all(14),
                              decoration: BoxDecoration(
                                color: AppColors.success.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(10),
                                border: Border.all(
                                  color:
                                      AppColors.success.withValues(alpha: 0.3),
                                ),
                              ),
                              child: Row(children: [
                                const Icon(Icons.check_circle_rounded,
                                    color: AppColors.success, size: 18),
                                const Gap(10),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      const Text(
                                        'Exported!',
                                        style: TextStyle(
                                          color: AppColors.success,
                                          fontWeight: FontWeight.w600,
                                          fontSize: 13,
                                        ),
                                      ),
                                      const Gap(2),
                                      Text(
                                        state.exportedPath!,
                                        style: TextStyle(
                                          color: AppColors.textSecondary(b),
                                          fontSize: 10,
                                        ),
                                        maxLines: 2,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ],
                                  ),
                                ),
                                const Gap(8),
                                IconButton(
                                  icon: const Icon(Icons.share_rounded,
                                      size: 18, color: AppColors.primary),
                                  onPressed: () => Share.shareXFiles(
                                      [XFile(state.exportedPath!)]),
                                ),
                              ]),
                            ),
                          ],

                          if (state.error != null) ...[
                            const Gap(12),
                            ErrorBar(message: state.error!),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // Bottom action — Save plan
              StepBottomButton(
                label: 'Save Plan',
                icon: Icons.bookmark_rounded,
                brightness: b,
                onPressed: () {
                  Navigator.pop(context);
                },
              ),
              const Gap(12),
            ],
          ),
        );
      },
    );
  }
}
