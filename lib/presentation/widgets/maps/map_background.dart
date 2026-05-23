import 'package:flutter/material.dart';
import 'persistent_map.dart';

/// Replaces the old grid-painted placeholder with the real Mapbox map.
/// Used as the full-screen background in Setup, Waypoints, and Generating steps.
class MapBackground extends StatelessWidget {
  // brightness kept for API compatibility but the map handles styling itself
  final Brightness brightness;
  const MapBackground({super.key, required this.brightness});

  @override
  Widget build(BuildContext context) {
    return const Positioned.fill(child: PersistentMap());
  }
}
