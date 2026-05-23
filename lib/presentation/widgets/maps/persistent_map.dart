import 'package:flutter/material.dart';
import 'live_map.dart';

/// A [GlobalKey] shared across all steps so the [MapboxMap] widget is
/// never destroyed and rebuilt when we navigate between steps.
/// Import this key wherever the map is used as the body.
final liveMapKey = GlobalKey<State<LiveMap>>();

/// Drop this widget anywhere you want the persistent Mapbox map.
/// Because [liveMapKey] is global, Flutter reuses the same widget instance.
class PersistentMap extends StatelessWidget {
  const PersistentMap({super.key});

  @override
  Widget build(BuildContext context) {
    return LiveMap(key: liveMapKey);
  }
}
