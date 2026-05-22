import 'dart:convert';

import 'package:dartz/dartz.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/errors/failures.dart';
import '../../../../domain/entities/entities.dart';
import '../../../../domain/repositories/repositories.dart';

class PlanRepositoryImpl implements PlanRepository {
  final SharedPreferences _prefs;
  final _uuid = const Uuid();

  PlanRepositoryImpl(this._prefs);

  @override
  Future<Either<Failure, RoutePlan>> buildPlan({
    required Route route,
    required int days,
    required double speedKmh,
    required DateTime startTime,
    required List<BreakType> breaks,
  }) async {
    try {
      final distPerDay = route.totalDistanceKm / days;
      final allBreaks = <RouteBreak>[];
      final segments = <DailySegment>[];

      for (int d = 0; d < days; d++) {
        final dayStart = startTime.add(Duration(days: d));
        final dayStartKm = d * distPerDay;
        final dayEndKm = (d + 1) * distPerDay;
        final dayBreaks = <RouteBreak>[];

        for (final bt in breaks) {
          // Skip overnight on last day
          if (bt == BreakType.overnight && d == days - 1) continue;

          final triggerKm = _breakTriggerKm(bt, dayStartKm, distPerDay);
          if (triggerKm > dayEndKm) continue;

          final travelSoFar = (triggerKm - dayStartKm) / speedKmh;
          final breakTime = dayStart.add(Duration(seconds: (travelSoFar * 3600).round()));
          final br = RouteBreak(
            id: _uuid.v4(),
            type: bt,
            scheduledAt: breakTime,
            duration: Duration(minutes: bt.defaultMinutes),
            distanceFromStartKm: triggerKm,
          );
          dayBreaks.add(br);
          allBreaks.add(br);
        }

        final movingSec = (distPerDay / speedKmh * 3600).round();
        final breakSec = dayBreaks.fold(0, (s, b) => s + b.duration.inSeconds);
        final arrivalTime = dayStart.add(Duration(seconds: movingSec + breakSec));

        segments.add(DailySegment(
          day: d + 1,
          startKm: dayStartKm,
          endKm: dayEndKm,
          breaks: dayBreaks,
          departureTime: dayStart,
          estimatedArrival: arrivalTime,
        ));
      }

      final movingH = route.totalDistanceKm / speedKmh;
      final breakSec = allBreaks.fold(0, (s, b) => s + b.duration.inSeconds);
      final total = Duration(seconds: (movingH * 3600).round() + breakSec);

      return Right(RoutePlan(
        id: _uuid.v4(),
        route: route,
        totalDays: days,
        speedKmh: speedKmh,
        startTime: startTime,
        breaks: allBreaks,
        segments: segments,
        estimatedTotal: total,
      ));
    } catch (e) {
      return Left(RouteFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> savePlan(RoutePlan plan) async {
    try {
      final all = _loadRaw();
      all[plan.id] = {
        'id': plan.id,
        'origin': plan.route.origin.name ?? plan.route.origin.toString(),
        'destination': plan.route.destination.name ?? plan.route.destination.toString(),
        'days': plan.totalDays,
        'distanceKm': plan.route.totalDistanceKm,
        'sport': plan.route.sport.name,
        'savedAt': DateTime.now().toIso8601String(),
      };
      await _prefs.setString(kSavedPlansKey, jsonEncode(all));
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<RoutePlan>>> loadPlans() async {
    return const Right([]);
  }

  @override
  Future<Either<Failure, void>> deletePlan(String id) async {
    try {
      final all = _loadRaw()..remove(id);
      await _prefs.setString(kSavedPlansKey, jsonEncode(all));
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure(e.toString()));
    }
  }

  double _breakTriggerKm(BreakType bt, double dayStart, double dayDist) {
    switch (bt) {
      case BreakType.breakfast:
        return dayStart + dayDist * 0.05;
      case BreakType.pitStop:
        return dayStart + dayDist * 0.25;
      case BreakType.lunch:
        return dayStart + dayDist * 0.50;
      case BreakType.supper:
        return dayStart + dayDist * 0.85;
      case BreakType.overnight:
        return dayStart + dayDist * 1.0;    // end of day
    }
  }

  Map<String, dynamic> _loadRaw() {
    final s = _prefs.getString(kSavedPlansKey);
    if (s == null) return {};
    return Map<String, dynamic>.from(jsonDecode(s) as Map);
  }
}
