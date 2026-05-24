import 'dart:math';

import '../constants/app_constants.dart';

class Fmt {
  Fmt._();

  static String distance(double km, DistanceUnit unit) {
    if (unit == DistanceUnit.miles) {
      final mi = km * 0.621371;
      return mi >= 10 ? '${mi.toStringAsFixed(1)} mi' : '${mi.toStringAsFixed(2)} mi';
    }
    if (km >= 1) return '${km.toStringAsFixed(1)} km';
    return '${(km * 1000).toStringAsFixed(0)} m';
  }

  static String elevation(double meters, DistanceUnit unit) {
    if (unit == DistanceUnit.miles) return '${(meters * 3.28084).toStringAsFixed(0)} ft';
    return '${meters.toStringAsFixed(0)} m';
  }

  static String speed(double kmh, DistanceUnit unit) {
    if (unit == DistanceUnit.miles) return '${(kmh * 0.621371).toStringAsFixed(1)} mph';
    return '${kmh.toStringAsFixed(1)} km/h';
  }

  static String pace(double kmh, DistanceUnit unit) {
    final paceMinPerKm = 60.0 / kmh;
    final paceMinPerUnit = unit == DistanceUnit.miles ? paceMinPerKm * 1.60934 : paceMinPerKm;
    final mins = paceMinPerUnit.floor();
    final secs = ((paceMinPerUnit - mins) * 60).round();
    return '$mins:${secs.toString().padLeft(2, '0')} /${unit.symbol}';
  }

  static String duration(Duration d) {
    final h = d.inHours;
    final m = d.inMinutes % 60;
    if (h == 0) return '${m}min';
    if (m == 0) return '${h}h';
    return '${h}h ${m}min';
  }

  static String hhmm(DateTime dt) {
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }

  static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    const r = 6371.0;
    final dLat = _rad(lat2 - lat1);
    final dLon = _rad(lon2 - lon1);
    final a = sin(dLat / 2) * sin(dLat / 2) +
        cos(_rad(lat1)) * cos(_rad(lat2)) * sin(dLon / 2) * sin(dLon / 2);
    return r * 2 * atan2(sqrt(a), sqrt(1 - a));
  }

  static double _rad(double deg) => deg * pi / 180;

  static double minSpeed(SportType s) {
    switch (s) {
      case SportType.running:
        return 4.0;
      case SportType.cycling:
        return 8.0;
      case SportType.skating:
        return 5.0;
      default:
        return 2.0;
    }
  }

  static double maxSpeed(SportType s) {
    switch (s) {
      case SportType.running:
        return 22.0;
      case SportType.cycling:
        return 50.0;
      case SportType.skating:
        return 30.0;
      default:
        return 8.0;
    }
  }

  static String speedLabel(SportType s, String end) {
    if (end == 'slow') return '${minSpeed(s).toStringAsFixed(0)} km/h';
    return '${maxSpeed(s).toStringAsFixed(0)} km/h';
  }
}
