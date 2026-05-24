import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:intl/intl.dart';

import '../../../core/utils/formatters.dart';
import '../../../domain/entities/route_plan.dart';
import '../../bloc/home/home_bloc.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../../widgets/elevation_chart.dart';
import '../../widgets/state_widgets.dart';
import '../../widgets/steps/day_card.dart';
import '../../widgets/steps/step_bottom_button.dart';
import '../planner/planner_screen.dart';

class PlanDetailScreen extends StatelessWidget {
  final RoutePlan plan;

  const PlanDetailScreen({super.key, required this.plan});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    final route = plan.route;
    final unit = route.unit;
    final now = DateTime.now();
    final isUpcoming = plan.startTime.isAfter(now);

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: AppBar(
        backgroundColor: AppColors.bg(b),
        elevation: 0,
        title: Text(
          '${route.origin.name ?? 'Plan'} → ${route.destination.name ?? ''}',
          style: TextStyle(
            color: AppColors.textPrimary(b),
            fontSize: 16,
            fontWeight: FontWeight.w700,
          ),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: AppColors.textPrimary(b)),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.delete_outline_rounded,
                color: AppColors.danger, size: 22),
            onPressed: () => _confirmDelete(context, b),
          ),
          const Gap(4),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header stats
                  Container(
                    color: AppColors.surface(b),
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                    child: Column(
                      children: [
                        Row(
                          children: [
                            Text(route.sport.emoji,
                                style: const TextStyle(fontSize: 22)),
                            const Gap(10),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    '${route.origin.name ?? 'Origin'} → ${route.destination.name ?? 'Destination'}',
                                    style: TextStyle(
                                      fontSize: 14,
                                      fontWeight: FontWeight.w700,
                                      color: AppColors.textPrimary(b),
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                  const Gap(3),
                                  Row(
                                    children: [
                                      Icon(Icons.calendar_today_rounded,
                                          size: 11,
                                          color: AppColors.textMuted(b)),
                                      const Gap(4),
                                      Text(
                                        DateFormat('EEE, MMM d, yyyy – h:mm a')
                                            .format(plan.startTime),
                                        style: TextStyle(
                                          fontSize: 11,
                                          color: AppColors.textSecondary(b),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 8, vertical: 3),
                              decoration: BoxDecoration(
                                color: isUpcoming
                                    ? AppColors.primary.withValues(alpha: 0.15)
                                    : AppColors.surface(b),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                isUpcoming ? 'Upcoming' : 'Past',
                                style: TextStyle(
                                  fontSize: 10,
                                  fontWeight: FontWeight.w700,
                                  color: isUpcoming
                                      ? AppColors.primary
                                      : AppColors.textMuted(b),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const Gap(14),
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
                                label: '${plan.totalDays} days',
                                value: Fmt.distance(
                                  route.totalDistance / plan.totalDays,
                                  unit,
                                ),
                              ),
                            ),
                          ],
                        ),
                        if (route.elevation.isNotEmpty) ...[
                          const Gap(12),
                          ElevationChart(
                            points: route.elevation,
                            unit: unit,
                            height: 80,
                          ),
                        ],
                      ],
                    ),
                  ),

                  // Daily segments
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Daily Schedule',
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                            color: AppColors.textPrimary(b),
                          ),
                        ),
                        const Gap(12),
                        ...plan.segments.map(
                          (seg) => Padding(
                            padding: const EdgeInsets.only(bottom: 10),
                            child: DayCard(
                              segment: seg,
                              unit: unit,
                              sport: route.sport,
                              brightness: b,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          StepBottomButton(
            label: 'Edit Plan',
            icon: Icons.edit_rounded,
            brightness: b,
            onPressed: () => _editPlan(context),
          ),
          const Gap(12),
        ],
      ),
    );
  }

  void _editPlan(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    bloc.add(ResetAll());
    bloc.add(SetImportedRoute(plan.route));
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => const PlannerScreen()),
    );
  }

  void _confirmDelete(BuildContext context, Brightness b) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface(b),
        title: Text(
          'Delete plan?',
          style: TextStyle(color: AppColors.textPrimary(b)),
        ),
        content: Text(
          'This cannot be undone.',
          style: TextStyle(color: AppColors.textSecondary(b)),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('Cancel',
                style: TextStyle(color: AppColors.textSecondary(b))),
          ),
          TextButton(
            onPressed: () {
              context.read<HomeBloc>().add(DeletePlan(plan.id));
              Navigator.pop(ctx);
              Navigator.pop(context);
            },
            child: const Text('Delete',
                style: TextStyle(color: AppColors.danger)),
          ),
        ],
      ),
    );
  }
}
