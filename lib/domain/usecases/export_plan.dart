import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../../core/constants/app_constants.dart';
import '../entities/route_plan.dart';
import '../repositories/repositories.dart';

class ExportPlan extends UseCase<String, ExportPlanParams> {
  final ExportRepository _repo;
  ExportPlan(this._repo);

  @override
  Future<Either<Failure, String>> call(ExportPlanParams p) {
    switch (p.format) {
      case ExportFormat.gpx:
        return _repo.toGpx(p.plan);
      case ExportFormat.pdf:
        return _repo.toPdf(p.plan);
      case ExportFormat.image:
        return _repo.toImage(p.plan);
    }
  }
}

class ExportPlanParams extends Equatable {
  final RoutePlan plan;
  final ExportFormat format;

  const ExportPlanParams({required this.plan, required this.format});

  @override
  List<Object?> get props => [plan, format];
}
