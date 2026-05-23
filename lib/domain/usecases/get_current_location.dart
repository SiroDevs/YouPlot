import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../entities/location.dart';
import '../repositories/repositories.dart';

class GetCurrentLocation extends NoParamUseCase<Location> {
  final LocationRepository _repo;
  GetCurrentLocation(this._repo);

  @override
  Future<Either<Failure, Location>> call() => _repo.getCurrentLocation();
}
