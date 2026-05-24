import 'dart:convert';

import 'package:dio/dio.dart';

import '../../core/constants/app_constants.dart';
import '../../core/utils/osm_utils.dart';
import '../../domain/entities/elevation_point.dart';

Future<List<ElevationPoint>> fetchElevationIsolate(
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
