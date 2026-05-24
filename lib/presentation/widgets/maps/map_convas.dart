import 'package:flutter/material.dart';

import '../../../domain/entities/route_map.dart';
import '../steps/step_header.dart';
import '../steps/badges.dart';
import 'map_background.dart';

class MapCanvas extends StatelessWidget {
  final RouteMap route;
  final Brightness brightness;
  final bool isDark;
  final VoidCallback onBack;
  final VoidCallback onReset;

  const MapCanvas({
    super.key,
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
        const Positioned.fill(child: PersistentMap()),

        Positioned(
          top: 0, left: 0, right: 0,
          child: StepHeader(
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
