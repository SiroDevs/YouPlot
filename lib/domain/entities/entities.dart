import 'package:equatable/equatable.dart';

import '../../core/constants/app_constants.dart';

class Location extends Equatable {
  final double lat;
  final double lng;
  final String? name;
  final String? address;

  const Location({required this.lat, required this.lng, this.name, this.address});

  @override
  List<Object?> get props => [lat, lng];

  @override
  String toString() => name ?? address ?? '$lat,$lng';
}

class ElevationPoint extends Equatable {
  final double distanceKm;
  final double elevationM;

  const ElevationPoint({required this.distanceKm, required this.elevationM});

  @override
  List<Object?> get props => [distanceKm, elevationM];
}

class Waypoint extends Equatable {
  final String id;
  final Location location;
  final String label;
  final double? distanceFromStartKm;

  const Waypoint({
    required this.id,
    required this.location,
    required this.label,
    this.distanceFromStartKm,
  });

  @override
  List<Object?> get props => [id];
}

class Route extends Equatable {
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

  const Route({
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

  double get minElevM => elevation.isEmpty ? 0 : elevation.map((e) => e.elevationM).reduce((a, b) => a < b ? a : b);
  double get maxElevM => elevation.isEmpty ? 0 : elevation.map((e) => e.elevationM).reduce((a, b) => a > b ? a : b);

  @override
  List<Object?> get props => [id];
}

class RouteBreak extends Equatable {
  final String id;
  final BreakType type;
  final DateTime scheduledAt;
  final Duration duration;
  final double? distanceFromStartKm;

  const RouteBreak({
    required this.id,
    required this.type,
    required this.scheduledAt,
    required this.duration,
    this.distanceFromStartKm,
  });

  @override
  List<Object?> get props => [id];
}

class DailySegment extends Equatable {
  final int day; 
  final double startKm;
  final double endKm;
  final List<RouteBreak> breaks;
  final DateTime departureTime;
  final DateTime estimatedArrival;

  const DailySegment({
    required this.day,
    required this.startKm,
    required this.endKm,
    required this.breaks,
    required this.departureTime,
    required this.estimatedArrival,
  });

  double get distanceKm => endKm - startKm;

  @override
  List<Object?> get props => [day];
}

class RoutePlan extends Equatable {
  final String id;
  final Route route;
  final int totalDays;
  final double speedKmh;
  final DateTime startTime;
  final List<RouteBreak> breaks;
  final List<DailySegment> segments;
  final Duration estimatedTotal;

  const RoutePlan({
    required this.id,
    required this.route,
    required this.totalDays,
    required this.speedKmh,
    required this.startTime,
    required this.breaks,
    required this.segments,
    required this.estimatedTotal,
  });

  @override
  List<Object?> get props => [id];
}
