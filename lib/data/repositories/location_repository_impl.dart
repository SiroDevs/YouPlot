import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../domain/entities/location.dart';
import '../../domain/repositories/location_repository.dart';
import '../datasources/location_datasource.dart';
import '../datasources/osm_datasource.dart';

class LocationRepositoryImpl implements LocationRepository {
  final LocationDatasource _gps;
  final OsmDatasource _osm;
  LocationRepositoryImpl(this._gps, this._osm);

  @override
  Future<Either<Failure, Location>> getCurrentLocation() async {
    try {
      return Right(await _gps.getCurrentLocation());
    } on LocationException catch (e) {
      return Left(LocationFailure(e.message));
    } catch (e) {
      return Left(LocationFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<Location>>> searchPlaces(String query) async {
    try {
      return Right(await _osm.searchPlaces(query));
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } catch (e) {
      return Left(GeocodingFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Location>> reverseGeocode(
      double lat, double lng) async {
    try {
      return Right(await _osm.reverseGeocode(lat, lng));
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } catch (e) {
      return Left(GeocodingFailure(e.toString()));
    }
  }
}
