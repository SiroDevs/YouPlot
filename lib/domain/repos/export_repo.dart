import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/route_plan.dart';

abstract class ExportRepo {
  Future<Either<Failure, String>> toGpx(RoutePlan plan);
  Future<Either<Failure, String>> toPdf(RoutePlan plan);
  Future<Either<Failure, String>> toImage(RoutePlan plan);
}
