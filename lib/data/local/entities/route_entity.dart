import 'package:froom/froom.dart';

@Entity(tableName: 'routes')
class RouteEntity {
  @PrimaryKey()
  final String id;

  final String origin;
  final double originLat;
  final double originLng;

  final String destination;
  final double destinationLat;
  final double destinationLng;

  final double totalDistance;
  final double totalAscent;
  final double totalDescent;
  final String sport;
  final String unit;
  final String routeJson;
  final int createdAt;

  const RouteEntity({
    required this.id,
    required this.origin,
    required this.originLat,
    required this.originLng,
    required this.destination,
    required this.destinationLat,
    required this.destinationLng,
    required this.totalDistance,
    required this.totalAscent,
    required this.totalDescent,
    required this.sport,
    required this.unit,
    required this.routeJson,
    required this.createdAt,
  });
}
