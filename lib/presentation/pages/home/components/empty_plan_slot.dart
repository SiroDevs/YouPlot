import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../bloc/home/home_bloc.dart';
import '../../../theme/app_colors.dart';

class EmptyPlanSlot extends StatelessWidget {
  final PlanFilter filter;
  final Brightness brightness;

  const EmptyPlanSlot({
    super.key,
    required this.filter,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final msg = switch (filter) {
      PlanFilter.upcoming => 'No upcoming plans',
      PlanFilter.past => 'No past plans',
      PlanFilter.all => 'No plans yet',
    };
    final sub = switch (filter) {
      PlanFilter.upcoming => 'Create a plan to get started',
      PlanFilter.past => 'Completed plans will appear here',
      PlanFilter.all => 'Tap + New Plan to create one',
    };

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 36, horizontal: 20),
      decoration: BoxDecoration(
        color: AppColors.card(b),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Column(
        children: [
          Icon(Icons.calendar_month_outlined,
              size: 36, color: AppColors.textMuted(b)),
          const Gap(12),
          Text(
            msg,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary(b),
            ),
          ),
          const Gap(4),
          Text(
            sub,
            style: TextStyle(fontSize: 12, color: AppColors.textMuted(b)),
          ),
        ],
      ),
    );
  }
}
