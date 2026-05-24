import 'dart:math';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

import '../../core/constants/app_constants.dart';
import '../../core/errors/failures.dart';
import '../../core/utils/osm_utils.dart';
import '../../domain/entities/elevation_point.dart';
import '../../domain/entities/location.dart';
import 'elevation_isolate.dart';

class OsmDatasource {
  final Dio _dio;
  OsmDatasource(this._dio);

  Map<String, String> get _nominatimHeaders => {
    'User-Agent': dotenv.env['OSM_USER_AGENT']!,
    'Accept': '*/*',
  };

  Future<List<Location>> searchPlaces(
    String query, {
    double? nearLat,
    double? nearLng,
    double radiusKm = 50,
  }) async {
    try {
      final params = <String, dynamic>{
        'q': query,
        'format': 'jsonv2',
        'limit': '8',
        'addressdetails': '1',
        'extratags': '1',
        'namedetails': '1',
      };

      if (nearLat != null && nearLng != null) {
        final dLat = radiusKm / 111.0;
        final dLng = radiusKm / (111.0 * cos(nearLat * pi / 180));
        params['viewbox'] =
            '${(nearLng - dLng).toStringAsFixed(4)},'
            '${(nearLat + dLat).toStringAsFixed(4)},'
            '${(nearLng + dLng).toStringAsFixed(4)},'
            '${(nearLat - dLat).toStringAsFixed(4)}';
        params['bounded'] = '0';
      }

      final resp = await _dio.get(
        '$kNominatimBase/search',
        queryParameters: params,
        options: Options(headers: _nominatimHeaders),
      );

      final features = resp.data as List;

      return features
          .map((f) => locationDto(f as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw NetworkException(e.message ?? 'Geocoding request failed');
    }
  }

  Future<Location> reverseGeocode(double lat, double lng) async {
    try {
      final resp = await _dio.get(
        '$kNominatimBase/reverse',
        queryParameters: {
          'lat': lat.toString(),
          'lon': lng.toString(),
          'format': 'jsonv2',
          'addressdetails': '1',
        },
        options: Options(headers: _nominatimHeaders),
      );

      if (resp.data is! Map) return Location(lat: lat, lng: lng);
      return locationDto(
        resp.data as Map<String, dynamic>,
        forcedLat: lat,
        forcedLng: lng,
      );
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
      final waypoints = [
        origin,
        ...via,
        destination,
      ].map((p) => '${p.lng},${p.lat}').join(';');

      final resp = await _dio.get(
        '$kOsrmBase/$profile/$waypoints',
        queryParameters: {
          'geometries': 'geojson',
          'overview': 'full',
          'steps': 'false',
        },
        options: Options(
          sendTimeout: const Duration(seconds: 20),
          receiveTimeout: const Duration(seconds: 30),
        ),
      );

      if (resp.data['code'] != 'Ok' || (resp.data['routes'] as List).isEmpty) {
        throw RouteException('No routes found between these points');
      }

      final route = resp.data['routes'][0] as Map<String, dynamic>;
      final distM = (route['distance'] as num).toDouble();
      final coords2d = (route['geometry']['coordinates'] as List)
          .map((c) => <double>[(c as List)[0].toDouble(), c[1].toDouble()])
          .toList();

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
    return compute(fetchElevationIsolate, sampled);
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

