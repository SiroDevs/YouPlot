import 'dart:math';

import '../../domain/entities/location.dart';

class DirectionsResult {
  final List<List<double>> geometry;
  final double distanceKm;
  DirectionsResult({required this.geometry, required this.distanceKm});
}

double haversineDist(double lat1, double lon1, double lat2, double lon2) {
  const r = 6371.0;
  final dLat = _rad(lat2 - lat1);
  final dLon = _rad(lon2 - lon1);
  final a =
      sin(dLat / 2) * sin(dLat / 2) +
      cos(_rad(lat1)) * cos(_rad(lat2)) * sin(dLon / 2) * sin(dLon / 2);
  return r * 2 * atan2(sqrt(a), sqrt(1 - a));
}

double _rad(double d) => d * pi / 180;

class OsmFmt {
  static double haversineKm(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) => haversineDist(lat1, lon1, lat2, lon2);
}

Location locationDto(
  Map<String, dynamic> f, {
  double? forcedLat,
  double? forcedLng,
}) {
  final lat = forcedLat ?? double.parse(f['lat'].toString());
  final lng = forcedLng ?? double.parse(f['lon'].toString());

  final addr = f['address'] as Map<String, dynamic>?;
  final name =
      f['name'] as String? ??
      f['namedetails']?['name'] as String? ??
      addr?['amenity'] as String? ??
      addr?['road'] as String? ??
      addr?['suburb'] as String? ??
      addr?['city'] as String? ??
      addr?['town'] as String? ??
      addr?['village'] as String? ??
      '';

  final city =
      addr?['city'] as String? ??
      addr?['town'] as String? ??
      addr?['village'] as String?;
  final road = addr?['road'] as String?;
  final country = addr?['country'] as String?;

  final addressParts = [road, city, country].whereType<String>().toList();

  return Location(
    lat: lat,
    lng: lng,
    name: name.isNotEmpty
        ? name
        : (f['display_name'] as String?)?.split(',').first.trim(),
    address: addressParts.isNotEmpty
        ? addressParts.join(', ')
        : f['display_name'] as String?,
  );
}
