import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../core/constants/app_constants.dart';
import '../entities/location.dart';
import '../entities/route_map.dart';
import '../entities/waypoint.dart';

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
