import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/route_map.dart';
import '../../../../domain/entities/route_plan.dart';
import '../../../bloc/review/review_cubit.dart';
import '../../../bloc/route_builder/route_session_cubit.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/icon_text_button.dart';
import '../../../widgets/steps/step_header.dart';
import '../widgets/daily_plan_tab.dart';
import '../widgets/export_tab.dart';

class PlanStep5 extends StatefulWidget {
  const PlanStep5({super.key});

  @override
  State<PlanStep5> createState() => _PlanStep5State();
}

class _PlanStep5State extends State<PlanStep5>
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
    final cubit = context.read<ReviewCubit>();

    return BlocBuilder<ReviewCubit, ReviewState>(
      builder: (ctx, reviewState) {
        final session = ctx.read<RouteSessionCubit>().state;
        final plan = session.plan!;
        final route = plan.route;
        final unit = session.unit;
        final sport = session.sport;
        final b = Theme.of(ctx).brightness;

        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Column(
            children: [
              StepHeader(
                showBack: true,
                onBack: cubit.goBack,
                stepNumber: 4,
                totalSteps: 5,
              ),

              _PlanStatsHeader(
                plan: plan,
                route: route,
                unit: unit,
                tabs: _tabs,
                brightness: b,
              ),

              TabBarView(
                controller: _tabs,
                children: [
                  DailyPlanTab(
                    plan: plan,
                    unit: unit,
                    sport: sport,
                    brightness: b,
                  ),
                  ExportTab(
                    reviewState: reviewState,
                    brightness: b,
                    onExport: cubit.export,
                  ),
                ],
              ).expanded(),

              IconTextButton(
                label: 'Save Plan',
                icon: Icons.bookmark_rounded,
                brightness: b,
                onPressed: () => Navigator.pop(context),
              ),
              const Gap(12),
            ],
          ),
        );
      },
    );
  }
}

class _PlanStatsHeader extends StatelessWidget {
  final RoutePlan plan;
  final RouteMap route;
  final DistanceUnit unit;
  final TabController tabs;
  final Brightness brightness;

  const _PlanStatsHeader({
    required this.plan,
    required this.route,
    required this.unit,
    required this.tabs,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;

    return Container(
      color: AppColors.surface(b),
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      child: Column(
        children: [
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
                label: 'Avg/day',
                value: Fmt.distance(
                  route.totalDistance / plan.totalDays,
                  unit,
                ),
              ).expanded(),
            ],
          ),
          const Gap(12),
          ElevationChart(points: route.elevation, unit: unit, height: 80),
          const Gap(4),
          TabBar(
            controller: tabs,
            labelColor: AppColors.primary,
            unselectedLabelColor: AppColors.textSecondary(b),
            indicatorColor: AppColors.primary,
            labelStyle: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
            ),
            tabs: const [
              Tab(text: 'Daily Plan'),
              Tab(text: 'Export'),
            ],
          ),
        ],
      ),
    );
  }
}
