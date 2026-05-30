import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/route_map.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/state_widgets.dart';

class RoutePanel extends StatelessWidget {
  final RouteMap route;
  final Brightness brightness;
  final bool isDark;
  final VoidCallback onPlan;

  const RoutePanel({
    super.key,
    required this.route,
    required this.brightness,
    required this.isDark,
    required this.onPlan,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final unit = route.unit;
    final sport = route.sport;

    return Container(
      decoration: BoxDecoration(
        color: isDark
            ? Colors.black.withValues(alpha: 0.9)
            : Colors.white.withValues(alpha: 0.97),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.3),
            blurRadius: 16,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 4,
            margin: const EdgeInsets.only(top: 10),
            decoration: BoxDecoration(
              color: AppColors.border(b),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const Gap(14),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                StatCard(
                  label: 'Distance',
                  value: Fmt.distance(route.totalDistance, unit),
                  icon: Icons.straighten_rounded,
                  color: AppColors.sport(sport),
                ).expanded(),
                const Gap(8),
                StatCard(
                  label: 'Ascent',
                  value: '+${Fmt.elevation(route.totalAscent, unit)}',
                  icon: Icons.trending_up_rounded,
                  color: AppColors.warning,
                ).expanded(),
                const Gap(8),
                StatCard(
                  label: 'Descent',
                  value: '-${Fmt.elevation(route.totalDescent, unit)}',
                  icon: Icons.trending_down_rounded,
                  color: AppColors.accent,
                ).expanded(),
              ],
            ),
          ),

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Elevation profile',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary(b),
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const Gap(6),
                ElevationChart(
                  points: route.elevation,
                  unit: unit,
                  height: 100,
                ),
              ],
            ),
          ),

          if (route.waypoints.isNotEmpty)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: route.waypoints
                      .map(
                        (w) => Container(
                          margin: const EdgeInsets.only(right: 8),
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.card(b),
                            borderRadius: BorderRadius.circular(6),
                            border: Border.all(
                              color: AppColors.border(b),
                              width: 0.5,
                            ),
                          ),
                          child: Text(
                            w.label,
                            style: TextStyle(
                              fontSize: 11,
                              color: AppColors.textPrimary(b),
                            ),
                          ),
                        ),
                      )
                      .toList(),
                ),
              ),
            ),

          const Gap(16),

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 0),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: onPlan,
                icon: const Icon(Icons.tune_rounded, size: 16),
                label: const Text('Plan this route'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.all(14),
                ),
              ),
            ),
          ),

          SafeArea(top: false, child: const SizedBox(height: 12)),
        ],
      ),
    );
  }
}
