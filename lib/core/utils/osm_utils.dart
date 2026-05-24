
import 'dart:math';

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
