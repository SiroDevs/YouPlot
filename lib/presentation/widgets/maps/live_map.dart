// ignore_for_file: unused_field
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';

class LiveMap extends StatefulWidget {
  const LiveMap({super.key});

  @override
  State<LiveMap> createState() => LiveMapState();
}

class LiveMapState extends State<LiveMap> with TickerProviderStateMixin {
  final MapController _mapController = MapController();

  // Track last-drawn state to avoid redundant layer rebuilds.
  Location? _lastOrigin;
  Location? _lastDest;
  String? _lastRouteId;
  bool _tilesFaded = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        context.read<RouteBuilderBloc>().add(
          MapControllerReady(_mapController),
        );
        _flyToCurrentLocation();
      }
    });
  }

  Future<void> _flyToCurrentLocation() async {
    final locState = context.read<RouteBuilderBloc>().state;
    final origin = locState.origin;
    if (origin != null) {
      _mapController.move(LatLng(origin.lat, origin.lng), 13);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final tileUrl = isDark ? kOsmTileTemplateDark : kOsmTileTemplate;

    return BlocConsumer<RouteBuilderBloc, RouteBuilderState>(
      listenWhen: (prev, curr) =>
          prev.origin != curr.origin ||
          prev.destination != curr.destination ||
          prev.route?.id != curr.route?.id,
      listener: (ctx, state) => _onStateChange(state),
      buildWhen: (prev, curr) =>
          prev.origin != curr.origin ||
          prev.destination != curr.destination ||
          prev.route?.id != curr.route?.id ||
          prev.viaPoints.length != curr.viaPoints.length,
      builder: (ctx, state) {
        final markers = _buildMarkers(state);
        final polylines = _buildPolylines(state);

        return FlutterMap(
          mapController: _mapController,
          options: MapOptions(
            initialCenter: const LatLng(-1.2921, 36.8219),
            initialZoom: 5,
            minZoom: 3,
            maxZoom: 18,
            interactionOptions: const InteractionOptions(
              flags: InteractiveFlag.all,
            ),
            onMapReady: _onMapReady,
          ),
          children: [
            TileLayer(
              urlTemplate: tileUrl,
              subdomains: isDark
                  ? const ['a', 'b', 'c', 'd']
                  : const ['a', 'b', 'c'],
              userAgentPackageName: 'com.youplot.app',
              tileProvider: NetworkTileProvider(),
              tileBuilder: _fadeTileBuilder,
              errorTileCallback: (tile, err, _) {},
            ),

            if (polylines.isNotEmpty)
              PolylineLayer(polylines: polylines),
            MarkerLayer(markers: markers),
            RichAttributionWidget(
              attributions: [TextSourceAttribution(kAppCredits2, onTap: () {})],
              alignment: AttributionAlignment.bottomRight,
            ),
          ],
        );
      },
    );
  }

  Widget _fadeTileBuilder(BuildContext ctx, Widget tile, TileImage tileImage) {
    return AnimatedOpacity(
      opacity: tileImage.loadFinishedAt != null ? 1.0 : 0.0,
      duration: const Duration(milliseconds: 200),
      curve: Curves.easeIn,
      child: tile,
    );
  }

  List<Marker> _buildMarkers(RouteBuilderState state) {
    final markers = <Marker>[];

    if (state.origin != null) {
      markers.add(
        _pinMarker(
          state.origin!,
          color: AppColors.primary,
          icon: Icons.trip_origin_rounded,
          label: state.origin!.name,
        ),
      );
    }

    if (state.destination != null) {
      markers.add(
        _pinMarker(
          state.destination!,
          color: Colors.redAccent,
          icon: Icons.location_on_rounded,
          label: state.destination!.name,
        ),
      );
    }

    for (final via in state.viaPoints) {
      markers.add(
        _pinMarker(
          via,
          color: Colors.amber.shade700,
          icon: Icons.radio_button_checked_rounded,
          small: true,
        ),
      );
    }

    return markers;
  }

  Marker _pinMarker(
    Location loc, {
    required Color color,
    required IconData icon,
    String? label,
    bool small = false,
  }) {
    final size = small ? 28.0 : 38.0;
    return Marker(
      point: LatLng(loc.lat, loc.lng),
      width: size + (label != null ? 100 : 0),
      height: size + (label != null ? 22 : 0),
      alignment: Alignment.bottomCenter,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (label != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.9),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                label,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 10,
                  fontWeight: FontWeight.w600,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          if (label != null) const SizedBox(height: 2),
          Container(
            width: size,
            height: size,
            decoration: BoxDecoration(
              color: color,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: color.withValues(alpha: 0.4),
                  blurRadius: 8,
                  offset: const Offset(0, 3),
                ),
              ],
            ),
            child: Icon(icon, color: Colors.white, size: size * 0.52),
          ),
        ],
      ),
    );
  }

  List<Polyline> _buildPolylines(RouteBuilderState state) {
    if (state.route == null || state.route!.geometry.isEmpty) return [];

    final points = state.route!.geometry
        .map((c) => LatLng(c[1], c[0]))
        .toList();

    return [
      Polyline(
        points: points,
        color: Colors.black.withValues(alpha: 0.18),
        strokeWidth: 7,
        strokeJoin: StrokeJoin.round,
        strokeCap: StrokeCap.round,
      ),
      Polyline(
        points: points,
        color: AppColors.primary,
        strokeWidth: 4.5,
        strokeJoin: StrokeJoin.round,
        strokeCap: StrokeCap.round,
        gradientColors: [
          AppColors.primary,
          AppColors.primary.withValues(alpha: 0.75),
        ],
      ),
    ];
  }
  void _onMapReady() {
    _flyToCurrentLocation();
  }

  void _onStateChange(RouteBuilderState state) {
    _lastOrigin = state.origin;
    _lastDest = state.destination;
    _lastRouteId = state.route?.id;
  }
}
