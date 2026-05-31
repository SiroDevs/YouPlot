import 'package:flutter/material.dart';

import 'live_map.dart';

final liveMapKey = GlobalKey<LiveMapState>();

class PersistentMap extends StatelessWidget {
  const PersistentMap({super.key});

  @override
  Widget build(BuildContext context) {
    return LiveMap(key: liveMapKey);
  }
}

class MapBackground extends StatefulWidget {
  const MapBackground({super.key});

  @override
  State<MapBackground> createState() => _MapBackgroundState();
}

class _MapBackgroundState extends State<MapBackground> {
  final _mapKey = GlobalKey<LiveMapState>();

  @override
  Widget build(BuildContext context) {
    return Positioned.fill(
      child: LiveMap(key: _mapKey),
    );
  }
}
