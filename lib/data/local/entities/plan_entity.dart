import 'package:froom/froom.dart';

@Entity(tableName: 'plans')
class PlanEntity {
  @PrimaryKey()
  final String id;
  final String routeId;
  final String origin;
  final String destination;
  final double totalDistance;
  final String sport;
  final int totalDays;
  final double speed;
  final int startTime;
  final String planJson;
  final int createdAt;

  const PlanEntity({
    required this.id,
    required this.routeId,
    required this.origin,
    required this.destination,
    required this.totalDistance,
    required this.sport,
    required this.totalDays,
    required this.speed,
    required this.startTime,
    required this.planJson,
    required this.createdAt,
  });
}
