import 'dart:convert';

import 'package:dartz/dartz.dart';

import '../../core/constants/app_constants.dart';
import '../../core/errors/failures.dart';
import '../../domain/entities/daily_segment.dart';
import '../../domain/entities/elevation_point.dart';
import '../../domain/entities/location.dart';
import '../../domain/entities/route_break.dart';
import '../../domain/entities/route_map.dart';
import '../../domain/entities/route_plan.dart';
import '../../domain/entities/waypoint.dart';
import '../../domain/repositories/local_repository.dart';
import '../local/app_database.dart';
import '../local/entities/plan_entity.dart';
import '../local/entities/route_entity.dart';

class LocalRepositoryImpl implements LocalRepository {
  final AppDatabase _db;

  LocalRepositoryImpl(this._db);

  @override
  Future<Either<Failure, void>> saveRoute(RouteMap route) async {
    try {
      final entity = RouteEntity(
        id: route.id,
        origin: route.origin.name ?? '${route.origin.lat},${route.origin.lng}',
        originLat: route.origin.lat,
        originLng: route.origin.lng,
        destination:
            route.destination.name ?? '${route.destination.lat},${route.destination.lng}',
        destinationLat: route.destination.lat,
        destinationLng: route.destination.lng,
        totalDistance: route.totalDistance,
        totalAscent: route.totalAscent,
        totalDescent: route.totalDescent,
        sport: route.sport.name,
        unit: route.unit.name,
        routeJson: _routeToJson(route),
        createdAt: DateTime.now().millisecondsSinceEpoch,
      );
      await _db.routeDao.insertRoute(entity);
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<RouteMap>>> loadRoutes({int limit = 20}) async {
    try {
      final entities = await _db.routeDao.getRecent(limit);
      return Right(entities.map(_entityToRoute).toList());
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> deleteRoute(String id) async {
    try {
      await _db.routeDao.deleteById(id);
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> savePlanToDb(RoutePlan plan) async {
    try {
      final entity = PlanEntity(
        id: plan.id,
        routeId: plan.route.id,
        origin:
            plan.route.origin.name ?? '${plan.route.origin.lat},${plan.route.origin.lng}',
        destination: plan.route.destination.name ??
            '${plan.route.destination.lat},${plan.route.destination.lng}',
        totalDistance: plan.route.totalDistance,
        sport: plan.route.sport.name,
        totalDays: plan.totalDays,
        speed: plan.speed,
        startTime: plan.startTime.millisecondsSinceEpoch,
        planJson: _planToJson(plan),
        createdAt: DateTime.now().millisecondsSinceEpoch,
      );
      await _db.planDao.insertPlan(entity);
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<RoutePlan>>> loadPlansFromDb({int limit = 20}) async {
    try {
      final entities = await _db.planDao.getRecent(limit);
      return Right(entities.map(_entityToPlan).toList());
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<RoutePlan>>> loadUpcomingPlans() async {
    try {
      final entities =
          await _db.planDao.getUpcoming(DateTime.now().millisecondsSinceEpoch);
      return Right(entities.map(_entityToPlan).toList());
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<RoutePlan>>> loadPastPlans() async {
    try {
      final entities =
          await _db.planDao.getPast(DateTime.now().millisecondsSinceEpoch);
      return Right(entities.map(_entityToPlan).toList());
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> deletePlanFromDb(String id) async {
    try {
      await _db.planDao.deleteById(id);
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  String _routeToJson(RouteMap r) => jsonEncode({
        'id': r.id,
        'origin': _locToMap(r.origin),
        'destination': _locToMap(r.destination),
        'waypoints': r.waypoints
            .map((w) => {
                  'id': w.id,
                  'location': _locToMap(w.location),
                  'label': w.label,
                  'distanceFromStartKm': w.distanceFromStartKm,
                })
            .toList(),
        'geometry': r.geometry,
        'elevation': r.elevation
            .map((e) => {'distanceKm': e.distanceKm, 'elevationM': e.elevationM})
            .toList(),
        'totalDistance': r.totalDistance,
        'totalAscent': r.totalAscent,
        'totalDescent': r.totalDescent,
        'sport': r.sport.name,
        'unit': r.unit.name,
      });

  RouteMap _entityToRoute(RouteEntity e) {
    final m = jsonDecode(e.routeJson) as Map<String, dynamic>;
    return _mapToRoute(m);
  }

  RouteMap _mapToRoute(Map<String, dynamic> m) {
    final sport = SportType.values.firstWhere(
      (s) => s.name == (m['sport'] as String),
      orElse: () => SportType.cycling,
    );
    final unit = DistanceUnit.values.firstWhere(
      (u) => u.name == (m['unit'] as String),
      orElse: () => DistanceUnit.kilometers,
    );
    return RouteMap(
      id: m['id'] as String,
      origin: _mapToLoc(m['origin'] as Map<String, dynamic>),
      destination: _mapToLoc(m['destination'] as Map<String, dynamic>),
      waypoints: (m['waypoints'] as List)
          .map((w) => Waypoint(
                id: w['id'] as String,
                location: _mapToLoc(w['location'] as Map<String, dynamic>),
                label: w['label'] as String,
                distanceFromStartKm: (w['distanceFromStartKm'] as num?)?.toDouble(),
              ))
          .toList(),
      geometry: (m['geometry'] as List)
          .map((g) => (g as List).map((v) => (v as num).toDouble()).toList())
          .toList(),
      elevation: (m['elevation'] as List)
          .map((e) => ElevationPoint(
                distanceKm: (e['distanceKm'] as num).toDouble(),
                elevationM: (e['elevationM'] as num).toDouble(),
              ))
          .toList(),
      totalDistance: (m['totalDistance'] as num).toDouble(),
      totalAscent: (m['totalAscent'] as num).toDouble(),
      totalDescent: (m['totalDescent'] as num).toDouble(),
      sport: sport,
      unit: unit,
    );
  }

  String _planToJson(RoutePlan p) => jsonEncode({
        'id': p.id,
        'route': jsonDecode(_routeToJson(p.route)),
        'totalDays': p.totalDays,
        'speed': p.speed,
        'startTime': p.startTime.toIso8601String(),
        'estimatedTotalSeconds': p.estimatedTotal.inSeconds,
        'breaks': p.breaks
            .map((b) => {
                  'id': b.id,
                  'type': b.type.name,
                  'scheduledAt': b.scheduledAt.toIso8601String(),
                  'durationMinutes': b.duration.inMinutes,
                  'distanceFromStartKm': b.distanceFromStartKm,
                })
            .toList(),
        'segments': p.segments
            .map((s) => {
                  'day': s.day,
                  'startKm': s.startKm,
                  'endKm': s.endKm,
                  'departureTime': s.departureTime.toIso8601String(),
                  'estimatedArrival': s.estimatedArrival.toIso8601String(),
                  'breaks': s.breaks
                      .map((b) => {
                            'id': b.id,
                            'type': b.type.name,
                            'scheduledAt': b.scheduledAt.toIso8601String(),
                            'durationMinutes': b.duration.inMinutes,
                            'distanceFromStartKm': b.distanceFromStartKm,
                          })
                      .toList(),
                })
            .toList(),
      });

  RoutePlan _entityToPlan(PlanEntity e) {
    final m = jsonDecode(e.planJson) as Map<String, dynamic>;
    final route = _mapToRoute(m['route'] as Map<String, dynamic>);

    RouteBreak _mapBreak(Map<String, dynamic> b) => RouteBreak(
          id: b['id'] as String,
          type: BreakType.values.firstWhere((t) => t.name == b['type']),
          scheduledAt: DateTime.parse(b['scheduledAt'] as String),
          duration: Duration(minutes: b['durationMinutes'] as int),
          distanceFromStartKm: (b['distanceFromStartKm'] as num?)?.toDouble(),
        );

    final segments = (m['segments'] as List)
        .map((s) => DailySegment(
              day: s['day'] as int,
              startKm: (s['startKm'] as num).toDouble(),
              endKm: (s['endKm'] as num).toDouble(),
              departureTime: DateTime.parse(s['departureTime'] as String),
              estimatedArrival: DateTime.parse(s['estimatedArrival'] as String),
              breaks:
                  (s['breaks'] as List).map((b) => _mapBreak(b as Map<String, dynamic>)).toList(),
            ))
        .toList();

    return RoutePlan(
      id: m['id'] as String,
      route: route,
      totalDays: m['totalDays'] as int,
      speed: (m['speed'] as num).toDouble(),
      startTime: DateTime.parse(m['startTime'] as String),
      estimatedTotal: Duration(seconds: m['estimatedTotalSeconds'] as int),
      breaks:
          (m['breaks'] as List).map((b) => _mapBreak(b as Map<String, dynamic>)).toList(),
      segments: segments,
    );
  }

  Map<String, dynamic> _locToMap(Location l) => {
        'lat': l.lat,
        'lng': l.lng,
        'name': l.name,
        'address': l.address,
      };

  Location _mapToLoc(Map<String, dynamic> m) => Location(
        lat: (m['lat'] as num).toDouble(),
        lng: (m['lng'] as num).toDouble(),
        name: m['name'] as String?,
        address: m['address'] as String?,
      );
}
