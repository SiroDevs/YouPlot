import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/route_plan.dart';
import '../../../bloc/home/home_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/steps/day_card.dart';

class PlanDailySchedule extends StatelessWidget {
  final RoutePlan plan;
  final Brightness brightness;

  const PlanDailySchedule({
    super.key,
    required this.plan,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final route = plan.route;

    return Padding(
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
                unit: route.unit,
                sport: route.sport,
                brightness: b,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class PlanDeleteDialog {
  static Future<void> show(
    BuildContext context, {
    required RoutePlan plan,
    required Brightness brightness,
  }) {
    final b = brightness;
    return showDialog(
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
            child: Text(
              'Cancel',
              style: TextStyle(color: AppColors.textSecondary(b)),
            ),
          ),
          TextButton(
            onPressed: () {
              context.read<HomeBloc>().add(DeletePlan(plan.id));
              Navigator.pop(ctx);
              Navigator.pop(context);
            },
            child: const Text(
              'Delete',
              style: TextStyle(color: AppColors.danger),
            ),
          ),
        ],
      ),
    );
  }
}