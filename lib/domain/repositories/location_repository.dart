import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/location.dart';

abstract class LocationRepository {
  Future<Either<Failure, Location>> getCurrentLocation();
  Future<Either<Failure, List<Location>>> searchPlaces(String query);
  Future<Either<Failure, Location>> reverseGeocode(double lat, double lng);
}
