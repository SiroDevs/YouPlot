import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../../../core/errors/failures.dart';
import '../../../../core/usecases/usecase.dart';
import '../../core/constants/app_constants.dart';
import '../entities/entities.dart';
import '../repositories/repositories.dart';

class BuildPlan extends UseCase<RoutePlan, BuildPlanParams> {
  final PlanRepository _repo;
  BuildPlan(this._repo);

  @override
  Future<Either<Failure, RoutePlan>> call(BuildPlanParams p) => _repo.buildPlan(
        route: p.route,
        days: p.days,
        speedKmh: p.speedKmh,
        startTime: p.startTime,
        breaks: p.breaks,
      );
}

class BuildPlanParams extends Equatable {
  final Route route;
  final int days;
  final double speedKmh;
  final DateTime startTime;
  final List<BreakType> breaks;

  const BuildPlanParams({
    required this.route,
    required this.days,
    required this.speedKmh,
    required this.startTime,
    required this.breaks,
  });

  @override
  List<Object?> get props => [route, days, speedKmh, startTime];
}
