import 'package:equatable/equatable.dart';

import 'daily_segment.dart';
import 'route_break.dart';
import 'route_map.dart';

class RoutePlan extends Equatable {
  final String id;
  final RouteMap route;
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
