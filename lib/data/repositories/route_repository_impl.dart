import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';

import '../../core/constants/app_constants.dart';
import '../../core/errors/failures.dart';
import '../../core/utils/formatters.dart';
import '../../domain/entities/elevation_point.dart';
import '../../domain/entities/location.dart';
import '../../domain/entities/route_map.dart';
import '../../domain/entities/waypoint.dart';
import '../../domain/repositories/route_repository.dart';
import '../datasources/osm_datasource.dart';

class RouteRepositoryImpl implements RouteRepository {
  final OsmDatasource _osm;
  final _uuid = const Uuid();

  RouteRepositoryImpl(this._osm);

  @override
  Future<Either<Failure, RouteMap>> buildRoute({
    required Location origin,
    required Location destination,
    required List<Location> viaPoints,
    required SportType sport,
    required DistanceUnit unit,
  }) async {
    try {
      final dir = await _osm.getDirections(
        origin: origin,
        destination: destination,
        via: viaPoints,
        profile: sport.osmrProfile,
      );

      final results = await Future.wait([
        _osm.getElevationProfile(dir.geometry, 100),
        _reverseGeocodeWaypoints(viaPoints),
      ]);

      final elev = results[0] as List<ElevationPoint>;
      final waypoints = results[1] as List<Waypoint>;

      final stats = await compute(_calcElevStats, elev);

      return Right(RouteMap(
        id: _uuid.v4(),
        origin: origin,
        destination: destination,
        waypoints: waypoints,
        geometry: dir.geometry,
        elevation: elev,
        totalDistance: dir.distanceKm,
        totalAscent: stats.$1,
        totalDescent: stats.$2,
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
      final pts = List.generate(4, (i) {
        final t = (i + 1) / 5.0;
        return Location(
          lat: origin.lat + (destination.lat - origin.lat) * t,
          lng: origin.lng + (destination.lng - origin.lng) * t,
        );
      });

      final resolved = await Future.wait(pts.map((pt) async {
        try {
          return await _osm.reverseGeocode(pt.lat, pt.lng);
        } catch (_) {
          return pt;
        }
      }));

      final waypoints = <Waypoint>[];
      for (int i = 0; i < resolved.length; i++) {
        final loc = resolved[i];
        waypoints.add(Waypoint(
          id: _uuid.v4(),
          location: loc,
          label: loc.name ??
              loc.address ??
              '${loc.lat.toStringAsFixed(3)}, ${loc.lng.toStringAsFixed(3)}',
          distanceFromStartKm:
              Fmt.haversineKm(origin.lat, origin.lng, loc.lat, loc.lng),
        ));
      }
      return Right(waypoints);
    } catch (e) {
      return Left(RouteFailure(e.toString()));
    }
  }

  Future<List<Waypoint>> _reverseGeocodeWaypoints(
      List<Location> userVia) async {
    final result = <Waypoint>[];
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

(double, double) _calcElevStats(List<ElevationPoint> elev) {
  double asc = 0, desc = 0;
  for (int i = 1; i < elev.length; i++) {
    final d = elev[i].elevationM - elev[i - 1].elevationM;
    if (d > 0) asc += d;
    if (d < 0) desc -= d;
  }
  return (asc, desc);
}
