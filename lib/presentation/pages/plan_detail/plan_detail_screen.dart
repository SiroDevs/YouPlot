import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../domain/entities/route_plan.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../../widgets/steps/step_bottom_button.dart';
import '../planner/planner_screen.dart';
import 'widgets/plan_header_stats.dart';
import 'widgets/plan_widgets.dart';

class PlanDetailScreen extends StatelessWidget {
  final RoutePlan plan;

  const PlanDetailScreen({super.key, required this.plan});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: _buildAppBar(context, b),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  PlanHeaderStats(plan: plan, brightness: b),
                  PlanDailySchedule(plan: plan, brightness: b),
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

  AppBar _buildAppBar(BuildContext context, Brightness b) {
    final route = plan.route;
    return AppBar(
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
          onPressed: () => PlanDeleteDialog.show(context, plan: plan, brightness: b),
        ),
        const Gap(4),
      ],
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
}