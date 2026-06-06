import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../../core/constants/app_constants.dart';
import '../entities/route_map.dart';
import '../entities/route_plan.dart';
import '../repos/plan_repository.dart';

class BuildPlan extends UseCase<RoutePlan, BuildPlanParams> {
  final PlanRepo _repo;
  BuildPlan(this._repo);

  @override
  Future<Either<Failure, RoutePlan>> call(BuildPlanParams p) => _repo.buildPlan(
        route: p.route,
        days: p.days,
        speed: p.speed,
        startTime: p.startTime,
        breaks: p.breaks,
      );
}

class BuildPlanParams extends Equatable {
  final RouteMap route;
  final int days;
  final double speed;
  final DateTime startTime;
  final List<BreakType> breaks;

  const BuildPlanParams({
    required this.route,
    required this.days,
    required this.speed,
    required this.startTime,
    required this.breaks,
  });

  @override
  List<Object?> get props => [route, days, speed, startTime];
}
