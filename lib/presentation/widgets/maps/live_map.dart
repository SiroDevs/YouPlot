// ignore_for_file: unused_field
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';

class LiveMap extends StatefulWidget {
  const LiveMap({super.key});

  @override
  State<LiveMap> createState() => LiveMapState();
}

class LiveMapState extends State<LiveMap> {
  MapboxMap? _ctrl;
  PointAnnotationManager? _pointAnnotationManager;   // ← concrete type
  PolylineAnnotationManager? _polylineManager;       // ← concrete type

  String? _drawnOriginId;
  String? _drawnDestId;
  String? _drawnRouteId;
  Location? _lastOrigin;
  Location? _lastDest;
  String? _lastRouteId;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final style = isDark ? kMapboxStyleDark : kMapboxStyleOutdoors;

    return BlocListener<RouteBuilderBloc, RouteBuilderState>(
      listenWhen: (prev, curr) =>
          prev.origin != curr.origin ||
          prev.destination != curr.destination ||
          prev.route != curr.route,
      listener: (ctx, state) => _syncAnnotations(state),
      child: MapWidget(
        styleUri: style,
        cameraOptions: CameraOptions(
          center: Point(coordinates: Position(36.8219, -1.2921)), // Nairobi
          zoom: 4,
        ),
        onMapCreated: _onMapCreated,
        onStyleLoadedListener: (_) => _onStyleLoaded(),
      ),
    );
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  Future<void> _onMapCreated(MapboxMap ctrl) async {
    _ctrl = ctrl;

    await ctrl.location.updateSettings(LocationComponentSettings(
      enabled: true,
      pulsingEnabled: true,
      pulsingColor: AppColors.primary.value,
    ));

    if (mounted) {
      context.read<RouteBuilderBloc>().add(MapControllerReady(ctrl));
    }
  }

  Future<void> _onStyleLoaded() async {
    final ctrl = _ctrl;
    if (ctrl == null) return;

    // Concrete types — no cast needed
    _pointAnnotationManager ??=
        await ctrl.annotations.createPointAnnotationManager();
    _polylineManager ??=
        await ctrl.annotations.createPolylineAnnotationManager();

    await _flyToUserOrWorld(ctrl);

    if (mounted) {
      final state = context.read<RouteBuilderBloc>().state;
      await _syncAnnotations(state);
    }
  }

  // ── Camera init ────────────────────────────────────────────────────────────

  Future<void> _flyToUserOrWorld(MapboxMap ctrl) async {
    try {
      await ctrl.location.updateSettings(LocationComponentSettings(
        enabled: true,
        pulsingEnabled: true,
        pulsingColor: AppColors.primary.value,
        locationPuck: LocationPuck(
          locationPuck2D: DefaultLocationPuck2D(),
        ),
      ));
    } catch (_) {}
  }

  // ── Annotations ────────────────────────────────────────────────────────────

  Future<void> _syncAnnotations(RouteBuilderState state) async {
    final mgr  = _pointAnnotationManager;  // already PointAnnotationManager?
    final poly = _polylineManager;         // already PolylineAnnotationManager?
    if (mgr == null || poly == null) return;

    // Origin pin
    if (state.origin != _lastOrigin) {
      if (_drawnOriginId != null) {
        await mgr.deleteAll();
        _drawnOriginId = null;
        _drawnDestId = null;
      }
      _lastOrigin = state.origin;
      if (state.origin != null) {
        final ann = await mgr.create(PointAnnotationOptions(
          geometry: Point(
              coordinates: Position(state.origin!.lng, state.origin!.lat)),
          iconSize: 1.4,
          textField: '🟢',
          textSize: 22,
          textOffset: [0, -1.2],
        ));
        _drawnOriginId = ann.id;
      }
    }

    // Destination pin
    if (state.destination != _lastDest) {
      _lastDest = state.destination;
      if (state.destination != null) {
        final ann = await mgr.create(PointAnnotationOptions(
          geometry: Point(
              coordinates:
                  Position(state.destination!.lng, state.destination!.lat)),
          textField: '🔴',
          textSize: 22,
          textOffset: [0, -1.2],
        ));
        _drawnDestId = ann.id;
      }
    }

    // Route polyline
    if (state.route?.id != _lastRouteId) {
      _lastRouteId = state.route?.id;
      await poly.deleteAll();
      _drawnRouteId = null;

      if (state.route != null && state.route!.geometry.isNotEmpty) {
        final positions =
            state.route!.geometry.map((c) => Position(c[0], c[1])).toList();

        final ann = await poly.create(PolylineAnnotationOptions(
          geometry: LineString(coordinates: positions),
          lineColor: AppColors.primary.value,
          lineWidth: 4.5,
          lineOpacity: 0.9,
          lineJoin: LineJoin.ROUND,
        ));
        _drawnRouteId = ann.id;
      }
    }
  }
}