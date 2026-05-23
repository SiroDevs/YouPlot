import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../../core/constants/app_constants.dart';
import '../entities/location.dart';
import '../entities/route_map.dart';
import '../repositories/route_repository.dart';

class BuildRoute extends UseCase<RouteMap, BuildRouteParams> {
  final RouteRepository _repo;
  BuildRoute(this._repo);

  @override
  Future<Either<Failure, RouteMap>> call(BuildRouteParams p) => _repo.buildRoute(
        origin: p.origin,
        destination: p.destination,
        viaPoints: p.viaPoints,
        sport: p.sport,
        unit: p.unit,
      );
}

class BuildRouteParams extends Equatable {
  final Location origin;
  final Location destination;
  final List<Location> viaPoints;
  final SportType sport;
  final DistanceUnit unit;

  const BuildRouteParams({
    required this.origin,
    required this.destination,
    required this.viaPoints,
    required this.sport,
    required this.unit,
  });

  @override
  List<Object?> get props => [origin, destination, sport, unit];
}
