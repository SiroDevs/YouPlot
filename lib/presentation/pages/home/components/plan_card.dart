import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/route_plan.dart';
import '../../../theme/app_colors.dart';

class PlanCard extends StatelessWidget {
  final RoutePlan plan;
  final Brightness brightness;
  final int index;

  const PlanCard({super.key, 
    required this.plan,
    required this.brightness,
    required this.index,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final now = DateTime.now();
    final isUpcoming = plan.startTime.isAfter(now);
    final daysLabel = plan.totalDays == 1 ? '1 day' : '${plan.totalDays} days';

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.card(b),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: AppColors.primaryDim,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Center(
              child: Text(
                plan.route.sport.emoji,
                style: const TextStyle(fontSize: 22),
              ),
            ),
          ),
          const Gap(12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${plan.route.origin.name ?? 'Origin'} → '
                  '${plan.route.destination.name ?? 'Destination'}',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary(b),
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const Gap(3),
                Row(
                  children: [
                    Icon(Icons.calendar_today_rounded,
                        size: 11, color: AppColors.textMuted(b)),
                    const Gap(4),
                    Text(
                      _formatDate(plan.startTime),
                      style: TextStyle(
                          fontSize: 11, color: AppColors.textSecondary(b)),
                    ),
                    const Gap(8),
                    Icon(Icons.timelapse_rounded,
                        size: 11, color: AppColors.textMuted(b)),
                    const Gap(4),
                    Text(
                      daysLabel,
                      style: TextStyle(
                          fontSize: 11, color: AppColors.textSecondary(b)),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const Gap(8),
          Container(
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
                color: isUpcoming
                    ? AppColors.primary
                    : AppColors.textMuted(b),
              ),
            ),
          ),
        ],
      ),
    ).animate().fadeIn(delay: Duration(milliseconds: 50 * index));
  }

  String _formatDate(DateTime dt) {
    final months = [
      'Jan','Feb','Mar','Apr','May','Jun',
      'Jul','Aug','Sep','Oct','Nov','Dec',
    ];
    return '${months[dt.month - 1]} ${dt.day}, ${dt.year}';
  }
}
