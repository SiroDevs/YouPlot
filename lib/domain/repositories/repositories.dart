import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../core/constants/app_constants.dart';
import '../entities/location.dart';
import '../entities/route_map.dart';
import '../entities/route_plan.dart';
import '../entities/waypoint.dart';

abstract class LocationRepository {
  Future<Either<Failure, Location>> getCurrentLocation();
  Future<Either<Failure, List<Location>>> searchPlaces(String query);
  Future<Either<Failure, Location>> reverseGeocode(double lat, double lng);
}

abstract class RouteRepository {
  Future<Either<Failure, RouteMap>> buildRoute({
    required Location origin,
    required Location destination,
    required List<Location> viaPoints,
    required SportType sport,
    required DistanceUnit unit,
  });

  Future<Either<Failure, List<Waypoint>>> suggestWaypoints({
    required Location origin,
    required Location destination,
    required SportType sport,
  });
}

abstract class PlanRepository {
  Future<Either<Failure, RoutePlan>> buildPlan({
    required RouteMap route,
    required int days,
    required double speedKmh,
    required DateTime startTime,
    required List<BreakType> breaks,
  });

  Future<Either<Failure, void>> savePlan(RoutePlan plan);
  Future<Either<Failure, List<RoutePlan>>> loadPlans();
  Future<Either<Failure, void>> deletePlan(String id);
}

abstract class ExportRepository {
  Future<Either<Failure, String>> toGpx(RoutePlan plan);
  Future<Either<Failure, String>> toPdf(RoutePlan plan);
  Future<Either<Failure, String>> toImage(RoutePlan plan);
}
