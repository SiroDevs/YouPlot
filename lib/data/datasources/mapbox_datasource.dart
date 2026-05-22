import 'dart:convert';
import 'dart:math';

import 'package:dio/dio.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/errors/failures.dart';
import '../../../../domain/entities/entities.dart';

class MapboxDatasource {
  final Dio _dio;
  MapboxDatasource(this._dio);

  Future<List<Location>> searchPlaces(String query) async {
    try {
      final resp = await _dio.get(
        '$kMapboxGeocode/${Uri.encodeComponent(query)}.json',
        queryParameters: {
          'access_token': kMapboxToken,
          'types': 'place,locality,neighborhood,address',
          'limit': '8',
          'language': 'en',
        },
      );
      final features = (resp.data['features'] as List);
      return features
          .map((f) => _locationFromFeature(f as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw NetworkException(e.message ?? 'Geocoding request failed');
    }
  }

  Future<Location> reverseGeocode(double lat, double lng) async {
    try {
      final resp = await _dio.get(
        '$kMapboxGeocode/$lng,$lat.json',
        queryParameters: {
          'access_token': kMapboxToken,
          'types': 'place,locality',
          'limit': '1',
        },
      );
      final features = resp.data['features'] as List;
      if (features.isEmpty) return Location(lat: lat, lng: lng);
      return _locationFromFeature(features.first as Map<String, dynamic>);
    } on DioException catch (e) {
      throw NetworkException(e.message ?? 'Reverse geocode failed');
    }
  }

  Future<DirectionsResult> getDirections({
    required Location origin,
    required Location destination,
    required List<Location> via,
    required String profile,
  }) async {
    try {
      final coords = [
        origin,
        ...via,
        destination,
      ].map((p) => '${p.lng},${p.lat}').join(';');

      final resp = await _dio.get(
        '$kMapboxDirections/$profile/$coords',
        queryParameters: {
          'access_token': kMapboxToken,
          'geometries': 'geojson',
          'overview': 'full',
          'steps': 'false',
        },
      );

      if (resp.data['code'] != 'Ok' || (resp.data['routes'] as List).isEmpty) {
        throw RouteException('No routes found between these points');
      }

      final route = resp.data['routes'][0] as Map<String, dynamic>;
      final distM = (route['distance'] as num).toDouble();
      final coords2d = (route['geometry']['coordinates'] as List)
          .map((c) => [(c as List)[0].toDouble(), c[1].toDouble()])
          .toList()
          .cast<List<double>>();

      return DirectionsResult(geometry: coords2d, distanceKm: distM / 1000.0);
    } on DioException catch (e) {
      throw NetworkException(e.message ?? 'Directions request failed');
    }
  }

  Future<List<ElevationPoint>> getElevationProfile(
    List<List<double>> geometry,
    int samples,
  ) async {
    final sampled = _sampleGeometry(geometry, samples);
    final result = <ElevationPoint>[];
    double cumDist = 0;

    try {
      final body = {
        'locations': sampled
            .map((c) => {'latitude': c[1], 'longitude': c[0]})
            .toList(),
      };
      final resp = await _dio.post(
        'https://api.open-elevation.com/api/v1/lookup',
        data: jsonEncode(body),
        options: Options(headers: {'Content-Type': 'application/json'}),
      );

      final results = resp.data['results'] as List;
      for (int i = 0; i < results.length; i++) {
        if (i > 0) {
          cumDist += Fmt.haversineKm(
            sampled[i - 1][1],
            sampled[i - 1][0],
            sampled[i][1],
            sampled[i][0],
          );
        }
        result.add(
          ElevationPoint(
            distanceKm: cumDist,
            elevationM: (results[i]['elevation'] as num).toDouble(),
          ),
        );
      }
    } catch (_) {
      for (int i = 0; i < sampled.length; i++) {
        if (i > 0) {
          cumDist += Fmt.haversineKm(
            sampled[i - 1][1],
            sampled[i - 1][0],
            sampled[i][1],
            sampled[i][0],
          );
        }
        result.add(ElevationPoint(distanceKm: cumDist, elevationM: 0));
      }
    }

    return result;
  }

  Location _locationFromFeature(Map<String, dynamic> f) {
    final coords = f['geometry']['coordinates'] as List;
    return Location(
      lat: (coords[1] as num).toDouble(),
      lng: (coords[0] as num).toDouble(),
      name: f['text'] as String?,
      address: f['place_name'] as String?,
    );
  }

  List<List<double>> _sampleGeometry(List<List<double>> geom, int n) {
    if (geom.length <= n) return geom;
    final step = geom.length / n;
    return List.generate(
      n,
      (i) => geom[min((i * step).floor(), geom.length - 1)],
    );
  }
}

class DirectionsResult {
  final List<List<double>> geometry;
  final double distanceKm;
  DirectionsResult({required this.geometry, required this.distanceKm});
}

class Fmt {
  static double haversineKm(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) {
    const r = 6371.0;
    final dLat = _r(lat2 - lat1);
    final dLon = _r(lon2 - lon1);
    final a =
        sin(dLat / 2) * sin(dLat / 2) +
        cos(_r(lat1)) * cos(_r(lat2)) * sin(dLon / 2) * sin(dLon / 2);
    return r * 2 * atan2(sqrt(a), sqrt(1 - a));
  }

  static double _r(double d) => d * pi / 180;
}
