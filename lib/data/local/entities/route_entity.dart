import 'package:froom/froom.dart';

@Entity(tableName: 'routes')
class RouteEntity {
  @PrimaryKey()
  final String id;

  final String originName;
  final double originLat;
  final double originLng;

  final String destinationName;
  final double destinationLat;
  final double destinationLng;

  final double totalDistanceKm;
  final double totalAscentM;
  final double totalDescentM;
  final String sport;  // SportType.name
  final String unit;   // DistanceUnit.name

  /// Full serialised RouteMap JSON (geometry, waypoints, elevation)
  final String routeJson;

  /// Epoch milliseconds
  final int createdAt;

  const RouteEntity({
    required this.id,
    required this.originName,
    required this.originLat,
    required this.originLng,
    required this.destinationName,
    required this.destinationLat,
    required this.destinationLng,
    required this.totalDistanceKm,
    required this.totalAscentM,
    required this.totalDescentM,
    required this.sport,
    required this.unit,
    required this.routeJson,
    required this.createdAt,
  });
}
