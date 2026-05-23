import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../core/utils/formatters.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/app_header.dart';
import '../../../widgets/steps/day_card.dart';

class ReviewStep extends StatelessWidget {
  const ReviewStep({super.key});

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
              AppHeader(
                showBack: true,
                onBack: () => bloc.add(GoToStep(AppStep.plan)),
              ),
              Container(
                color: AppColors.surface(b),
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 14),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: StatCard(
                            label: 'Total distance',
                            value: Fmt.distance(route.totalDistanceKm, unit),
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
                              route.totalDistanceKm / plan.totalDays,
                              unit,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const Gap(14),
                    ElevationChart(
                      points: route.elevation,
                      unit: unit,
                      height: 90,
                    ),
                  ],
                ),
              ),
              Expanded(
                child: ListView(
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
              ),
              Container(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
                color: AppColors.surface(b),
                child: SafeArea(
                  top: false,
                  child: SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: () => bloc.add(GoToStep(AppStep.export)),
                      icon: const Icon(Icons.download_rounded, size: 16),
                      label: const Text('Export Route'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.all(14),
                      ),
                    ),
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
