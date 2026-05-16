import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../../../core/errors/failures.dart';
import '../../../../core/usecases/usecase.dart';

import '../../core/constants/app_constants.dart';
import '../entities/entities.dart';
import '../repositories/repositories.dart';

class GetCurrentLocation extends NoParamUseCase<Location> {
  final LocationRepository _repo;
  const GetCurrentLocation(this._repo);

  @override
  Future<Either<Failure, Location>> call() => _repo.getCurrentLocation();
}

class SearchPlaces extends UseCase<List<Location>, SearchPlacesParams> {
  final LocationRepository _repo;
  const SearchPlaces(this._repo);

  @override
  Future<Either<Failure, List<Location>>> call(SearchPlacesParams p) =>
      _repo.searchPlaces(p.query);
}

class SearchPlacesParams extends Equatable {
  final String query;
  const SearchPlacesParams(this.query);
  @override
  List<Object?> get props => [query];
}

class BuildRoute extends UseCase<Route, BuildRouteParams> {
  final RouteRepository _repo;
  const BuildRoute(this._repo);

  @override
  Future<Either<Failure, Route>> call(BuildRouteParams p) => _repo.buildRoute(
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

// lib/domain/usecases/suggest_waypoints.dart

class SuggestWaypoints extends UseCase<List<Waypoint>, SuggestWaypointsParams> {
  final RouteRepository _repo;
  const SuggestWaypoints(this._repo);

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

// lib/domain/usecases/build_plan.dart

class BuildPlan extends UseCase<RoutePlan, BuildPlanParams> {
  final PlanRepository _repo;
  const BuildPlan(this._repo);

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

class ExportPlan extends UseCase<String, ExportPlanParams> {
  final ExportRepository _repo;
  const ExportPlan(this._repo);

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
