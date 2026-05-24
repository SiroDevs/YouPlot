import 'dart:convert';
import 'dart:math';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

import '../../core/constants/app_constants.dart';
import '../../core/errors/failures.dart';
import '../../core/utils/osm_utils.dart';
import '../../domain/entities/elevation_point.dart';
import '../../domain/entities/location.dart';

class OsmDatasource {
  final Dio _dio;
  OsmDatasource(this._dio);

  Map<String, String> get _nominatimHeaders => {
    'User-Agent': dotenv.env['OSM_USER_AGENT']!,
    'Accept-Language': 'en',
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
          .map((f) => _locationFromNominatim(f as Map<String, dynamic>))
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
      return _locationFromNominatim(
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
    return compute(_fetchElevationIsolate, sampled);
  }

  Location _locationFromNominatim(
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
    final addressParts = <String>[road!, city!, country!];

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

  List<List<double>> _sampleGeometry(List<List<double>> geom, int n) {
    if (geom.length <= n) return geom;
    final step = geom.length / n;
    return List.generate(
      n,
      (i) => geom[min((i * step).floor(), geom.length - 1)],
    );
  }
}

Future<List<ElevationPoint>> _fetchElevationIsolate(
  List<List<double>> sampled,
) async {
  final result = <ElevationPoint>[];
  double cumDist = 0;

  try {
    final body = {
      'locations': sampled
          .map((c) => {'latitude': c[1], 'longitude': c[0]})
          .toList(),
    };

    final client = Dio(
      BaseOptions(
        connectTimeout: const Duration(seconds: 15),
        receiveTimeout: const Duration(seconds: 30),
      ),
    );

    final resp = await client.post(
      kElevationLookup,
      data: jsonEncode(body),
      options: Options(headers: {'Content-Type': 'application/json'}),
    );

    final results = resp.data['results'] as List;
    for (int i = 0; i < results.length; i++) {
      if (i > 0) {
        cumDist += haversineDist(
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
        cumDist += haversineDist(
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
