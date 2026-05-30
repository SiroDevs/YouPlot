import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../core/utils/formatters.dart';
import '../../../domain/entities/route_map.dart';
import '../../theme/app_colors.dart';
import '../../widgets/elevation_chart.dart';
import '../../widgets/state_widgets.dart';
import '../../widgets/steps/icon_text_button.dart';
import '../plan_maker/plan_maker_screen.dart';

class RouteDetailScreen extends StatelessWidget {
  final RouteMap route;

  const RouteDetailScreen({super.key, required this.route});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    final unit = route.unit;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: AppBar(
        backgroundColor: AppColors.bg(b),
        elevation: 0,
        title: Text(
          '${route.origin.name ?? 'Route'} → ${route.destination.name ?? ''}',
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
      ),
      body: Column(
        children: [
          SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  height: 280,
                  color: b == Brightness.dark
                      ? const Color(0xFF0D1B2A)
                      : const Color(0xFFDDE8C8),
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.map_rounded, size: 48, color: AppColors.textMuted(b)),
                        const Gap(8),
                        Text('Route map', style: TextStyle(color: AppColors.textMuted(b))),
                      ],
                    ),
                  ),
                ),

                Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(route.sport.emoji, style: const TextStyle(fontSize: 20)),
                          const Gap(8),
                          Text(
                            route.sport.label,
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                              color: AppColors.textPrimary(b),
                            ),
                          ),
                        ],
                      ),
                      const Gap(16),

                      Row(
                        children: [
                          StatCard(
                            label: 'Distance',
                            value: Fmt.distance(route.totalDistance, unit),
                          ).expanded(),
                          const Gap(8),
                          StatCard(
                            label: 'Ascent',
                            value: '+${Fmt.elevation(route.totalAscent, unit)}',
                          ).expanded(),
                          const Gap(8),
                          StatCard(
                            label: 'Descent',
                            value: '-${Fmt.elevation(route.totalDescent, unit)}',
                          ).expanded(),
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
                        ElevationChart(points: route.elevation, unit: unit, height: 110),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ).expanded(),

          IconTextButton(
            label: 'Create Plan with this Route',
            icon: Icons.calendar_today_rounded,
            brightness: b,
            onPressed: () {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(
                  builder: (_) => PlanMakerScreen(importedRoute: route),
                ),
              );
            },
          ),
          const Gap(12),
        ],
      ),
    );
  }
}
