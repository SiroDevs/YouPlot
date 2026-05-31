import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:intl/intl.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/route_plan.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/state_widgets.dart';

class PlanHeaderStats extends StatelessWidget {
  final RoutePlan plan;
  final Brightness brightness;

  const PlanHeaderStats({
    super.key,
    required this.plan,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final route = plan.route;
    final unit = route.unit;
    final isUpcoming = plan.startTime.isAfter(DateTime.now());

    return Container(
      color: AppColors.surface(b),
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
      child: Column(
        children: [
          _PlanTitleRow(plan: plan, brightness: b, isUpcoming: isUpcoming),
          const Gap(14),
          Row(
            children: [
              StatCard(
                label: 'Distance',
                value: Fmt.distance(route.totalDistance, unit),
              ).expanded(),
              const Gap(8),
              StatCard(
                label: 'Total time',
                value: Fmt.duration(plan.estimatedTotal),
              ).expanded(),
              const Gap(8),
              StatCard(
                label: '${plan.totalDays} days',
                value: Fmt.distance(route.totalDistance / plan.totalDays, unit),
              ).expanded(),
            ],
          ),
          if (route.elevation.isNotEmpty) ...[
            const Gap(12),
            ElevationChart(points: route.elevation, unit: unit, height: 80),
          ],
        ],
      ),
    );
  }
}

class _PlanTitleRow extends StatelessWidget {
  final RoutePlan plan;
  final Brightness brightness;
  final bool isUpcoming;

  const _PlanTitleRow({
    required this.plan,
    required this.brightness,
    required this.isUpcoming,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final route = plan.route;

    return Row(
      children: [
        Text(route.sport.emoji, style: const TextStyle(fontSize: 22)),
        const Gap(10),
        Column(
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
                Icon(
                  Icons.calendar_today_rounded,
                  size: 11,
                  color: AppColors.textMuted(b),
                ),
                const Gap(4),
                Text(
                  DateFormat(
                    'EEE, MMM d, yyyy – h:mm a',
                  ).format(plan.startTime),
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary(b),
                  ),
                ),
              ],
            ),
          ],
        ).expanded(),
        _StatusBadge(isUpcoming: isUpcoming, brightness: b),
      ],
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final bool isUpcoming;
  final Brightness brightness;

  const _StatusBadge({required this.isUpcoming, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
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
          color: isUpcoming ? AppColors.primary : AppColors.textMuted(b),
        ),
      ),
    );
  }
}
