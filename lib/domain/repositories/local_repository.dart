import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/route_map.dart';
import '../entities/route_plan.dart';

abstract class LocalRepository {
  Future<Either<Failure, void>> saveRoute(RouteMap route);
  Future<Either<Failure, List<RouteMap>>> loadRoutes({int limit = 20});
  Future<Either<Failure, void>> deleteRoute(String id);

  Future<Either<Failure, void>> savePlanToDb(RoutePlan plan);
  Future<Either<Failure, List<RoutePlan>>> loadPlansFromDb({int limit = 20});
  Future<Either<Failure, List<RoutePlan>>> loadUpcomingPlans();
  Future<Either<Failure, List<RoutePlan>>> loadPastPlans();
  Future<Either<Failure, void>> deletePlanFromDb(String id);
}
