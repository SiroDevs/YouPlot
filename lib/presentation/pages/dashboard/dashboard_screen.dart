import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../core/constants/app_constants.dart';
import '../../bloc/dashboard/dashboard_bloc.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../home/home_screen.dart';
import '../settings/settings_screen.dart';
import 'components/dashboard_content.dart';
import 'components/empty_state.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    context.read<DashboardBloc>().add(LoadDashboard());
  }

  void _goToPlanner() {
    context.read<RouteBuilderBloc>().add(ResetAll());
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const HomeScreen()),
    ).then((_) {
      if (mounted) context.read<DashboardBloc>().add(LoadDashboard());
    });
  }

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: _buildAppBar(context, b),
      body: BlocBuilder<DashboardBloc, DashboardState>(
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
            return EmptyState(onCreateNew: _goToPlanner, brightness: b);
          }

          return DashboardContent(
            state: state,
            brightness: b,
            onCreateNew: _goToPlanner,
          );
        },
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
