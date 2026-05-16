import 'package:equatable/equatable.dart';

sealed class Failure extends Equatable {
  final String message;
  const Failure(this.message);
  @override
  List<Object> get props => [message];
}

class NetworkFailure extends Failure {
  const NetworkFailure([super.message = 'Network error. Check your connection.']);
}

class LocationFailure extends Failure {
  const LocationFailure([super.message = 'Could not get location. Check permissions.']);
}

class RouteFailure extends Failure {
  const RouteFailure([super.message = 'Could not generate route between these points.']);
}

class GeocodingFailure extends Failure {
  const GeocodingFailure([super.message = 'Location not found.']);
}

class ExportFailure extends Failure {
  const ExportFailure([super.message = 'Export failed.']);
}

class CacheFailure extends Failure {
  const CacheFailure([super.message = 'Local storage error.']);
}

// lib/core/errors/exceptions.dart

class NetworkException implements Exception {
  final String message;
  const NetworkException([this.message = 'Network error']);
  @override
  String toString() => message;
}

class LocationException implements Exception {
  final String message;
  const LocationException([this.message = 'Location error']);
  @override
  String toString() => message;
}

class RouteException implements Exception {
  final String message;
  const RouteException([this.message = 'Route error']);
  @override
  String toString() => message;
}

class GeocodingException implements Exception {
  final String message;
  const GeocodingException([this.message = 'Geocoding error']);
  @override
  String toString() => message;
}
