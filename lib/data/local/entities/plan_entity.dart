import 'package:froom/froom.dart';

@Entity(tableName: 'saved_plans')
class PlanEntity {
  @PrimaryKey()
  final String id;

  final String routeId;

  final String originName;
  final String destinationName;
  final double totalDistanceKm;
  final String sport;

  final int totalDays;
  final double speedKmh;

  /// Epoch milliseconds of the first day's departure
  final int startTimeMs;

  /// Full serialised RoutePlan JSON (segments, breaks, etc.)
  final String planJson;

  /// Epoch milliseconds
  final int createdAt;

  const PlanEntity({
    required this.id,
    required this.routeId,
    required this.originName,
    required this.destinationName,
    required this.totalDistanceKm,
    required this.sport,
    required this.totalDays,
    required this.speedKmh,
    required this.startTimeMs,
    required this.planJson,
    required this.createdAt,
  });
}
