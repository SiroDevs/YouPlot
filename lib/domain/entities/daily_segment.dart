import 'package:equatable/equatable.dart';

import 'route_break.dart';

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
