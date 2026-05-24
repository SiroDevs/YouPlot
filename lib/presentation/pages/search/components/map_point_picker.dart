// ignore_for_file: unused_field
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:gap/gap.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../domain/entities/location.dart';
import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../planner/components/confirm_panel.dart';
import '../../planner/components/cross_hair.dart';
import '../../planner/components/glass_circle.dart';
import '../../planner/components/instruction_chip.dart';

class MapPointPicker extends StatefulWidget {
  final Brightness brightness;
  const MapPointPicker({super.key, required this.brightness});

  @override
  State<MapPointPicker> createState() => _MapPointPickerState();
}

class _MapPointPickerState extends State<MapPointPicker> {
  final MapController _mapController = MapController();

  LatLng? _pinnedPoint;
  bool _resolving = false;
  bool _mapReady = false;
  Marker? _tapMarker;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _initCamera());
  }

  void _initCamera() {
    if (!mounted) return;
    final rbState = context.read<RouteBuilderBloc>().state;
    final lsState = context.read<LocationSearchBloc>().state;

    final anchor = rbState.origin ??
        (lsState.currentLocation != null
            ? Location(
                lat: lsState.currentLocation!.lat,
                lng: lsState.currentLocation!.lng)
            : null);

    if (anchor != null) {
      _mapController.move(LatLng(anchor.lat, anchor.lng), 13);
    }
    setState(() => _mapReady = true);
  }

  @override
  Widget build(BuildContext context) {
    final b = widget.brightness;
    final isDark = b == Brightness.dark;

    final markers = <Marker>[
      if (_tapMarker != null) _tapMarker!,
    ];

    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(
              initialCenter: const LatLng(-1.2921, 36.8219),
              initialZoom: 5,
              minZoom: 3,
              maxZoom: 18,
              onTap: _onMapTap,
              interactionOptions: const InteractionOptions(
                flags: InteractiveFlag.all,
              ),
            ),
            children: [
              TileLayer(
                urlTemplate: kOsmTileTemplate,
                subdomains: isDark
                    ? const ['a', 'b', 'c', 'd']
                    : const ['a', 'b', 'c'],
                userAgentPackageName: 'com.youplot.app',
                tileProvider: NetworkTileProvider(),
                tileBuilder: _fadeTile,
              ),
              MarkerLayer(markers: markers),
              RichAttributionWidget(
                attributions: [
                  TextSourceAttribution(
                    '© OpenStreetMap contributors',
                    onTap: () {},
                  ),
                ],
                alignment: AttributionAlignment.bottomRight,
              ),
            ],
          ),

          if (_pinnedPoint == null) const Center(child: Crosshair()),

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
                    child: InstructionChip(
                      isDark: isDark,
                      hasPinned: _pinnedPoint != null,
                      resolving: _resolving,
                    ),
                  ),
                  const Gap(10),
                  GlassCircle(
                    child: BlocConsumer<LocationSearchBloc, LocationSearchState>(
                      listenWhen: (p, c) =>
                          p.currentLocation != c.currentLocation &&
                          c.currentLocation != null,
                      listener: (_, s) {
                        if (s.currentLocation != null) {
                          _mapController.move(
                            LatLng(s.currentLocation!.lat,
                                s.currentLocation!.lng),
                            14,
                          );
                        }
                      },
                      buildWhen: (p, c) => p.locating != c.locating,
                      builder: (_, s) => IconButton(
                        icon: s.locating
                            ? SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: AppColors.primary),
                              )
                            : Icon(Icons.my_location_rounded,
                                color: AppColors.primary, size: 20),
                        onPressed: s.locating
                            ? null
                            : () => context
                                .read<LocationSearchBloc>()
                                .add(LocateMe()),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          if (_pinnedPoint != null)
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: BlocBuilder<LocationSearchBloc, LocationSearchState>(
                buildWhen: (p, c) =>
                    p.reversedLocation != c.reversedLocation,
                builder: (_, s) {
                  final resolved = s.reversedLocation ??
                      Location(
                        lat: _pinnedPoint!.latitude,
                        lng: _pinnedPoint!.longitude,
                        name: _resolving
                            ? 'Resolving…'
                            : '${_pinnedPoint!.latitude.toStringAsFixed(4)}, '
                                '${_pinnedPoint!.longitude.toStringAsFixed(4)}',
                      );
                  return ConfirmPanel(
                    location: resolved,
                    brightness: b,
                    resolving: _resolving,
                    onConfirm: () => Navigator.pop(context, resolved),
                    onCancel: _clearPin,
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  Widget _fadeTile(BuildContext ctx, Widget tile, TileImage img) {
    return AnimatedOpacity(
      opacity: img.loadFinishedAt != null ? 1.0 : 0.0,
      duration: const Duration(milliseconds: 180),
      child: tile,
    );
  }

  void _onMapTap(TapPosition tapPosition, LatLng latLng) {
    context.read<LocationSearchBloc>().add(ClearSearch());

    setState(() {
      _pinnedPoint = latLng;
      _resolving = true;
      _tapMarker = _buildPinMarker(latLng);
    });

    _mapController.move(latLng, _mapController.camera.zoom);

    context
        .read<LocationSearchBloc>()
        .add(ReverseGeocode(latLng.latitude, latLng.longitude));

    _waitForReverseGeocode();
  }

  void _waitForReverseGeocode() {
    Future.delayed(const Duration(milliseconds: 3500), () {
      if (mounted && _resolving) setState(() => _resolving = false);
    });
  }

  void _clearPin() {
    context.read<LocationSearchBloc>().add(ClearSearch());
    setState(() {
      _pinnedPoint = null;
      _tapMarker = null;
      _resolving = false;
    });
  }

  Marker _buildPinMarker(LatLng point) {
    return Marker(
      point: point,
      width: 44,
      height: 54,
      alignment: Alignment.bottomCenter,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: AppColors.primary,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: AppColors.primary.withValues(alpha: 0.45),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: const Icon(Icons.push_pin_rounded,
                color: Colors.white, size: 22),
          ),
          Container(
            width: 2,
            height: 10,
            color: AppColors.primary,
          ),
        ],
      ),
    );
  }
}
