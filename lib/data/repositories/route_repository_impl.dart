import 'package:dartz/dartz.dart';
import 'package:uuid/uuid.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/errors/failures.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/entities.dart';
import '../../../../domain/repositories/repositories.dart';
import '../datasources/mapbox_datasource.dart' hide Fmt;

class RouteRepositoryImpl implements RouteRepository {
  final MapboxDatasource _mapbox;
  final _uuid = const Uuid();

  RouteRepositoryImpl(this._mapbox);

  @override
  Future<Either<Failure, Route>> buildRoute({
    required Location origin,
    required Location destination,
    required List<Location> viaPoints,
    required SportType sport,
    required DistanceUnit unit,
  }) async {
    try {
      final dir = await _mapbox.getDirections(
        origin: origin,
        destination: destination,
        via: viaPoints,
        profile: sport.mapboxProfile,
      );

      // Elevation (100 samples along geometry)
      final elev = await _mapbox.getElevationProfile(dir.geometry, 100);

      // Ascent / descent
      double asc = 0, desc = 0;
      for (int i = 1; i < elev.length; i++) {
        final d = elev[i].elevationM - elev[i - 1].elevationM;
        if (d > 0) asc += d;
        if (d < 0) desc -= d;
      }

      // Intermediate town labels from reverse geocoding
      final waypoints = await _reverseGeocodeWaypoints(dir.geometry, viaPoints);

      return Right(Route(
        id: _uuid.v4(),
        origin: origin,
        destination: destination,
        waypoints: waypoints,
        geometry: dir.geometry,
        elevation: elev,
        totalDistanceKm: dir.distanceKm,
        totalAscentM: asc,
        totalDescentM: desc,
        sport: sport,
        unit: unit,
      ));
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } on RouteException catch (e) {
      return Left(RouteFailure(e.message));
    } catch (e) {
      return Left(RouteFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<Waypoint>>> suggestWaypoints({
    required Location origin,
    required Location destination,
    required SportType sport,
  }) async {
    try {
      // Generate 4 intermediate points along great-circle path
      final pts = List.generate(4, (i) {
        final t = (i + 1) / 5.0;
        return Location(
          lat: origin.lat + (destination.lat - origin.lat) * t,
          lng: origin.lng + (destination.lng - origin.lng) * t,
        );
      });

      final waypoints = <Waypoint>[];
      for (final pt in pts) {
        try {
          final loc = await _mapbox.reverseGeocode(pt.lat, pt.lng);
          waypoints.add(Waypoint(
            id: _uuid.v4(),
            location: loc,
            label: loc.name ?? loc.address ?? '${pt.lat.toStringAsFixed(3)}, ${pt.lng.toStringAsFixed(3)}',
            distanceFromStartKm: Fmt.haversineKm(origin.lat, origin.lng, pt.lat, pt.lng),
          ));
        } catch (_) {
          waypoints.add(Waypoint(
            id: _uuid.v4(),
            location: pt,
            label: '${pt.lat.toStringAsFixed(3)}, ${pt.lng.toStringAsFixed(3)}',
            distanceFromStartKm: Fmt.haversineKm(origin.lat, origin.lng, pt.lat, pt.lng),
          ));
        }
      }
      return Right(waypoints);
    } catch (e) {
      return Left(RouteFailure(e.toString()));
    }
  }

  Future<List<Waypoint>> _reverseGeocodeWaypoints(
    List<List<double>> geometry,
    List<Location> userVia,
  ) async {
    final result = <Waypoint>[];

    // Use user-supplied via-points if any
    for (final loc in userVia) {
      result.add(Waypoint(
        id: _uuid.v4(),
        location: loc,
        label: loc.name ?? loc.toString(),
        distanceFromStartKm: null,
      ));
    }

    return result;
  }
}
