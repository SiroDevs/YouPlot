import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../bloc/dashboard/dashboard_bloc.dart';
import '../../../theme/app_colors.dart';

class PlanFilterChips extends StatelessWidget {
  final DashboardState state;
  const PlanFilterChips({super.key, required this.state});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: PlanFilter.values.map((f) {
        final active = state.planFilter == f;
        return Padding(
          padding: const EdgeInsets.only(left: 6),
          child: GestureDetector(
            onTap: () =>
                context.read<DashboardBloc>().add(SetPlanFilter(f)),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 160),
              padding:
                  const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(
                color: active ? AppColors.primary : Colors.transparent,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: active ? AppColors.primary : AppColors.border(
                    Theme.of(context).brightness,
                  ),
                  width: 1,
                ),
              ),
              child: Text(
                f.name[0].toUpperCase() + f.name.substring(1),
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                  color: active
                      ? Colors.white
                      : AppColors.textSecondary(Theme.of(context).brightness),
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}
