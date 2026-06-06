import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../../core/constants/app_constants.dart';
import '../entities/location.dart';
import '../entities/waypoint.dart';
import '../repos/route_repository.dart';

class SuggestWaypoints extends UseCase<List<Waypoint>, SuggestWaypointsParams> {
  final RouteRepo _repo;
  SuggestWaypoints(this._repo);

  @override
  Future<Either<Failure, List<Waypoint>>> call(SuggestWaypointsParams p) =>
      _repo.suggestWaypoints(
        origin: p.origin,
        destination: p.destination,
        sport: p.sport,
      );
}

class SuggestWaypointsParams extends Equatable {
  final Location origin;
  final Location destination;
  final SportType sport;

  const SuggestWaypointsParams({
    required this.origin,
    required this.destination,
    required this.sport,
  });

  @override
  List<Object?> get props => [origin, destination, sport];
}
