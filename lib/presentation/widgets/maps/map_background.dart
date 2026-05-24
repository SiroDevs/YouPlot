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
class MapBackground extends StatelessWidget {
  const MapBackground({super.key});

  @override
  Widget build(BuildContext context) {
    return const Positioned.fill(child: PersistentMap());
  }
}
