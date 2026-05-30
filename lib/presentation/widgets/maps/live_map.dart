// ignore_for_file: unused_field
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../bloc/route_builder/route_session_cubit.dart';
import '../../bloc/waypoints/waypoints_cubit.dart';
import '../../theme/app_colors.dart';

class LiveMap extends StatefulWidget {
  const LiveMap({super.key});

  @override
  State<LiveMap> createState() => LiveMapState();
}

class LiveMapState extends State<LiveMap> with TickerProviderStateMixin {
  final MapController _mapController = MapController();

  Location? _lastOrigin;
  Location? _lastDest;
  String? _lastRouteId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        // Register the map controller with the session so step cubits can
        // call fitCamera without needing a direct reference.
        context.read<RouteSessionCubit>().setMapController(_mapController);
        _flyToOrigin();
      }
    });
  }

  void _flyToOrigin() {
    final origin = context.read<RouteSessionCubit>().state.origin;
    if (origin != null) {
      _mapController.move(LatLng(origin.lat, origin.lng), 13);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final tileUrl = kOsmTileTemplate;

    // Listen to session for origin/destination/route changes.
    // Listen to waypoints cubit for via-point changes.
    return BlocConsumer<RouteSessionCubit, RouteSessionState>(
      listenWhen: (prev, curr) =>
          prev.origin != curr.origin ||
          prev.destination != curr.destination ||
          prev.route?.id != curr.route?.id,
      listener: (ctx, state) {
        _lastOrigin = state.origin;
        _lastDest = state.destination;
        _lastRouteId = state.route?.id;
      },
      buildWhen: (prev, curr) =>
          prev.origin != curr.origin ||
          prev.destination != curr.destination ||
          prev.route?.id != curr.route?.id,
      builder: (ctx, session) {
        return BlocBuilder<WaypointsCubit, WaypointsState>(
          buildWhen: (prev, curr) =>
              prev.viaPoints.length != curr.viaPoints.length,
          builder: (ctx, waypointState) {
            final markers = _buildMarkers(session, waypointState.viaPoints);
            final polylines = _buildPolylines(session);

            return FlutterMap(
              mapController: _mapController,
              options: MapOptions(
                initialCenter: const LatLng(-1.2921, 36.8219),
                initialZoom: 10,
                minZoom: 3,
                maxZoom: 18,
                interactionOptions: const InteractionOptions(
                  flags: InteractiveFlag.all,
                ),
              ),
              children: [
                TileLayer(
                  urlTemplate: tileUrl,
                  subdomains: isDark
                      ? const ['a', 'b', 'c', 'd']
                      : const ['a', 'b', 'c'],
                  userAgentPackageName: kAppPackage,
                  tileProvider: NetworkTileProvider(),
                  tileBuilder: _fadeTileBuilder,
                  errorTileCallback: (tile, err, _) {},
                ),
                if (polylines.isNotEmpty) PolylineLayer(polylines: polylines),
                MarkerLayer(markers: markers),
                RichAttributionWidget(
                  attributions: [
                    TextSourceAttribution(kAppCredits2, onTap: () {}),
                  ],
                  alignment: AttributionAlignment.bottomRight,
                ),
              ],
            );
          },
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

  List<Marker> _buildMarkers(
    RouteSessionState session,
    List<Location> viaPoints,
  ) {
    final markers = <Marker>[];

    if (session.origin != null) {
      markers.add(_pinMarker(
        session.origin!,
        color: AppColors.primary,
        icon: Icons.trip_origin_rounded,
        label: session.origin!.name,
      ));
    }

    if (session.destination != null) {
      markers.add(_pinMarker(
        session.destination!,
        color: Colors.redAccent,
        icon: Icons.location_on_rounded,
        label: session.destination!.name,
      ));
    }

    for (final via in viaPoints) {
      markers.add(_pinMarker(
        via,
        color: Colors.amber.shade700,
        icon: Icons.radio_button_checked_rounded,
        small: true,
      ));
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

  List<Polyline> _buildPolylines(RouteSessionState session) {
    if (session.route == null || session.route!.geometry.isEmpty) return [];

    final points = session.route!.geometry
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
}
