import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';
import '../../bloc/home/home_bloc.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../import/import_screen.dart';
import '../plan_maker/plan_maker_screen.dart';
import '../plan_detail/plan_detail_screen.dart';
import '../route_detail/route_detail_screen.dart';
import '../settings/settings_screen.dart';
import 'widgets/empty_state.dart';
import 'widgets/home_content.dart';
import 'widgets/home_dialog.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    context.read<HomeBloc>().add(LoadHome());
  }

  void _goToPlanner() {
    context.read<RouteBuilderBloc>().add(ResetAll());
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PlanMakerScreen()),
    ).then((_) {
      if (mounted) context.read<HomeBloc>().add(LoadHome());
    });
  }

  void _openRoute(RouteMap route) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => RouteDetailScreen(route: route)),
    ).then((_) {
      if (mounted) context.read<HomeBloc>().add(LoadHome());
    });
  }

  void _openPlan(RoutePlan plan) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => PlanDetailScreen(plan: plan)),
    ).then((_) {
      if (mounted) context.read<HomeBloc>().add(LoadHome());
    });
  }

  void _showNewPlanDialog() {
    showDialog(
      context: context,
      builder: (ctx) => NewPlanDialog(
        onScratch: () {
          Navigator.pop(ctx);
          _goToPlanner();
        },
        onImport: () {
          Navigator.pop(ctx);
          ImportScreen.show(context).then((_) {
            if (mounted) context.read<HomeBloc>().add(LoadHome());
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: _buildAppBar(context, b),
      floatingActionButton: _buildFab(b),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      body: BlocBuilder<HomeBloc, HomeState>(
        builder: (ctx, state) {
          if (state.loading) {
            return const Center(
              child: CircularProgressIndicator(
                strokeWidth: 2,
                color: AppColors.primary,
              ),
            );
          }

          if (state.isEmpty) {
            return EmptyState(brightness: b);
          }

          return HomeContent(
            state: state,
            brightness: b,
            onRouteSelected: _openRoute,
            onPlanSelected: _openPlan,
          );
        },
      ),
    );
  }

  Widget _buildFab(Brightness b) {
    return FloatingActionButton.extended(
      onPressed: _showNewPlanDialog,
      backgroundColor: AppColors.primary,
      foregroundColor: Colors.white,
      elevation: 4,
      icon: const Icon(Icons.add_rounded),
      label: const Text(
        'New Plan',
        style: TextStyle(fontWeight: FontWeight.w600),
      ),
    );
  }

  AppBar _buildAppBar(BuildContext context, Brightness b) {
    return AppBar(
      backgroundColor: AppColors.bg(b),
      elevation: 0,
      titleSpacing: 16,
      title: Row(
        children: [
          Container(
            width: 28,
            height: 28,
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(7),
            ),
            child: const Icon(Icons.route_rounded, size: 16, color: Colors.white),
          ),
          const Gap(10),
          Text(
            kAppName,
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
          ),
        ],
      ),
      actions: [
        IconButton(
          icon: Icon(Icons.settings_outlined,
              size: 22, color: AppColors.textSecondary(b)),
          onPressed: () => Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const SettingsScreen()),
          ),
        ),
        const Gap(4),
      ],
    );
  }
}
