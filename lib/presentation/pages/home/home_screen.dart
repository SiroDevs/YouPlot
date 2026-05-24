import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';
import '../../bloc/home/home_bloc.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../planner/planner_screen.dart';
import '../settings/settings_screen.dart';
import 'components/home_content.dart';
import 'components/empty_state.dart';
import '../import/import_screen.dart';
import 'plan_detail_screen.dart';
import 'route_detail_screen.dart';

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
      MaterialPageRoute(builder: (_) => const PlannerScreen()),
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
      builder: (ctx) => _NewPlanDialog(
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

class _NewPlanDialog extends StatelessWidget {
  final VoidCallback onScratch;
  final VoidCallback onImport;

  const _NewPlanDialog({required this.onScratch, required this.onImport});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Dialog(
      backgroundColor: AppColors.surface(b),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'New Plan',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
            ),
            const Gap(6),
            Text(
              'How would you like to start?',
              style: TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary(b),
              ),
            ),
            const Gap(20),
            _DialogOption(
              icon: Icons.edit_road_rounded,
              color: AppColors.primary,
              title: 'Plan from scratch',
              subtitle: 'Search locations and build your route',
              onTap: onScratch,
              brightness: b,
            ),
            const Gap(12),
            _DialogOption(
              icon: Icons.upload_file_rounded,
              color: const Color(0xFF6366F1),
              title: 'Import a file',
              subtitle: 'GPX, KML, TCX or FIT format',
              onTap: onImport,
              brightness: b,
            ),
            const Gap(8),
          ],
        ),
      ),
    );
  }
}

class _DialogOption extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final Brightness brightness;

  const _DialogOption({
    required this.icon,
    required this.color,
    required this.title,
    required this.subtitle,
    required this.onTap,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return InkWell(
      borderRadius: BorderRadius.circular(14),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppColors.card(b),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: AppColors.border(b), width: 0.5),
        ),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, size: 22, color: color),
            ),
            const Gap(14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary(b),
                    ),
                  ),
                  const Gap(2),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary(b),
                    ),
                  ),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded,
                size: 18, color: AppColors.textMuted(b)),
          ],
        ),
      ),
    );
  }
}
