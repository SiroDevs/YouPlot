import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/route_map.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import 'stat_chip.dart';

class DoneView extends StatelessWidget {
  final RouteMap route;
  final Brightness brightness;

  const DoneView({super.key, required this.route, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.success.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.success.withValues(alpha: 0.3)),
            ),
            child: Row(
              children: [
                const Icon(Icons.check_circle_rounded,
                    color: AppColors.success, size: 20),
                const Gap(10),
                Text(
                  'Route imported successfully!',
                  style: TextStyle(
                    color: AppColors.success,
                    fontWeight: FontWeight.w600,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
          ),
          const Gap(20),

          Text(
            '${route.origin.name ?? 'Start'} → ${route.destination.name ?? 'End'}',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary(b),
            ),
          ),
          const Gap(16),

          Row(
            children: [
              StatChip(
                icon: Icons.straighten_rounded,
                label: '${route.totalDistance.toStringAsFixed(1)} km',
                color: AppColors.primary,
              ),
              const Gap(8),
              StatChip(
                icon: Icons.trending_up_rounded,
                label: '+${route.totalAscent.round()}m',
                color: AppColors.warning,
              ),
              const Gap(8),
              StatChip(
                icon: Icons.trending_down_rounded,
                label: '-${route.totalDescent.round()}m',
                color: AppColors.accent,
              ),
            ],
          ),

          if (route.elevation.isNotEmpty) ...[
            const Gap(20),
            Text(
              'Elevation profile',
              style: TextStyle(
                fontSize: 12,
                color: AppColors.textSecondary(b),
                fontWeight: FontWeight.w500,
              ),
            ),
            const Gap(8),
            ElevationChart(
              points: route.elevation,
              unit: route.unit,
              height: 110,
            ),
          ],

          const Gap(20),
          Text(
            'Ready to plan your trip',
            style: TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary(b),
            ),
          ),
        ],
      ),
    );
  }
}
