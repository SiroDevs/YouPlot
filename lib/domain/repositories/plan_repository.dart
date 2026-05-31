import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../core/constants/app_constants.dart';
import '../entities/route_map.dart';
import '../entities/route_plan.dart';

abstract class PlanRepository {
  Future<Either<Failure, RoutePlan>> buildPlan({
    required RouteMap route,
    required int days,
    required double speed,
    required DateTime startTime,
    required List<BreakType> breaks,
  });

  Future<Either<Failure, void>> savePlan(RoutePlan plan);
  Future<Either<Failure, List<RoutePlan>>> loadPlans();
  Future<Either<Failure, void>> deletePlan(String id);
}
