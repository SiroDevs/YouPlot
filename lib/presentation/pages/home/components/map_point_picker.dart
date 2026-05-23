import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';
import 'package:gap/gap.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../domain/entities/location.dart';
import '../../../bloc/location_search/location_search_bloc.dart';
import 'confirm_panel.dart';
import 'glass_circle.dart';

class MapPointPicker extends StatefulWidget {
  final Brightness brightness;
  const MapPointPicker({super.key, required this.brightness});

  @override
  State<MapPointPicker> createState() => _MapPointPickerState();
}

class _MapPointPickerState extends State<MapPointPicker> {
  MapboxMap? _map;
  Location? _picked;
  bool _resolving = false;

  @override
  Widget build(BuildContext context) {
    final b = widget.brightness;
    final isDark = b == Brightness.dark;

    return Scaffold(
      body: Stack(
        children: [
          MapWidget(
            styleUri: isDark ? kMapboxStyleDark : kMapboxStyleOutdoors,
            onMapCreated: (map) => _map = map,
            onTapListener: _onMapTap,
          ),

          SafeArea(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(12, 12, 12, 0),
              child: Row(
                children: [
                  GlassCircle(
                    child: IconButton(
                      icon: const Icon(Icons.arrow_back_rounded),
                      color: isDark ? Colors.white : Colors.black87,
                      onPressed: () => Navigator.pop(context),
                    ),
                  ),
                  const Gap(10),
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 12,
                      ),
                      decoration: BoxDecoration(
                        color: isDark
                            ? Colors.black.withValues(alpha: 0.8)
                            : Colors.white.withValues(alpha: 0.95),
                        borderRadius: BorderRadius.circular(24),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.15),
                            blurRadius: 12,
                          ),
                        ],
                      ),
                      child: Text(
                        'Tap anywhere on the map',
                        style: TextStyle(
                          fontSize: 14,
                          color: isDark ? Colors.white70 : Colors.black54,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const Center(child: Crosshair()),

          if (_picked != null)
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: ConfirmPanel(
                location: _picked!,
                brightness: b,
                resolving: _resolving,
                onConfirm: () => Navigator.pop(context, _picked),
                onCancel: () => setState(() => _picked = null),
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _onMapTap(MapContentGestureContext ctx) async {
    final coord = ctx.point.coordinates;
    final lat = coord.lat.toDouble();
    final lng = coord.lng.toDouble();

    setState(() {
      _resolving = true;
      _picked = Location(lat: lat, lng: lng, name: 'Selected point');
    });

    // Add a marker annotation
    await _map?.annotations.createPointAnnotationManager().then((mgr) async {
      await mgr.deleteAll();
      await mgr.create(
        PointAnnotationOptions(
          geometry: Point(coordinates: Position(lng, lat)),
          iconSize: 1.2,
        ),
      );
    });

    // Reverse geocode via LocationSearchBloc
    if (mounted) {
      context.read<LocationSearchBloc>().add(ReverseGeocode(lat, lng));
    }
  }

  @override
  void initState() {
    super.initState();
    // Listen for reverse geocode result
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }
}
