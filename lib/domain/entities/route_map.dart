import 'package:equatable/equatable.dart';

import '../../core/constants/app_constants.dart';
import 'elevation_point.dart';
import 'location.dart';
import 'waypoint.dart';

class RouteMap extends Equatable {
  final String id;
  final Location origin;
  final Location destination;
  final List<Waypoint> waypoints;
  final List<List<double>> geometry;
  final List<ElevationPoint> elevation;
  final double totalDistanceKm;
  final double totalAscentM;
  final double totalDescentM;
  final SportType sport;
  final DistanceUnit unit;

  const RouteMap({
    required this.id,
    required this.origin,
    required this.destination,
    required this.waypoints,
    required this.geometry,
    required this.elevation,
    required this.totalDistanceKm,
    required this.totalAscentM,
    required this.totalDescentM,
    required this.sport,
    required this.unit,
  });

  double get minElevM => elevation.isEmpty
      ? 0
      : elevation.map((e) => e.elevationM).reduce((a, b) => a < b ? a : b);
  double get maxElevM => elevation.isEmpty
      ? 0
      : elevation.map((e) => e.elevationM).reduce((a, b) => a > b ? a : b);

  @override
  List<Object?> get props => [id];
}
