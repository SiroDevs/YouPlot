import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../domain/entities/route_map.dart';
import '../../theme/app_colors.dart';
import '../steps/badges.dart';
import '../steps/general.dart';
import '../steps/headers.dart';

class MapCanvas extends StatelessWidget {
  final RouteMap route;
  final Brightness brightness;
  final bool isDark;
  final VoidCallback onBack;
  final VoidCallback onReset;

  const MapCanvas({super.key, 
    required this.route,
    required this.brightness,
    required this.isDark,
    required this.onBack,
    required this.onReset,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Stack(
      children: [
        Positioned.fill(
          child: Container(
            color: isDark ? const Color(0xFF0D1B2A) : const Color(0xFFE8E0D8),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.map_rounded,
                      size: 64,
                      color: AppColors.primary.withValues(alpha: 0.3)),
                  const Gap(12),
                  Text(
                    '${route.origin.name ?? "Start"} → ${route.destination.name ?? "End"}',
                    textAlign: TextAlign.center,
                    style:
                        TextStyle(color: AppColors.textSecondary(b), fontSize: 13),
                  ),
                  const Gap(6),
                  Text('MapboxMap widget goes here',
                      style:
                          TextStyle(color: AppColors.textMuted(b), fontSize: 11)),
                ],
              ),
            ),
          ),
        ),

        Positioned.fill(
          child: CustomPaint(painter: RoutePainter(isDark: isDark)),
        ),

        Positioned(
          top: 0, left: 0, right: 0,
          child: AppHeader(
            showBack: true,
            onBack: onBack,
            showNew: true,
            onNew: onReset,
          ),
        ),

        Positioned(
          top: 110, left: 12,
          child: SportBadge(sport: route.sport, brightness: b),
        ),
      ],
    );
  }
}
