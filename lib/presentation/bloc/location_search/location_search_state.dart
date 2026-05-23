part of 'location_search_bloc.dart';

class LocationSearchState extends Equatable {
  final String query;
  final List<Location> results;
  final List<Location> history;
  final Location? currentLocation;
  final Location? reversedLocation; // result of ReverseGeocode
  final bool loading;
  final bool locating;
  final String? error;

  const LocationSearchState({
    this.query = '',
    this.results = const [],
    this.history = const [],
    this.currentLocation,
    this.reversedLocation,
    this.loading = false,
    this.locating = false,
    this.error,
  });

  LocationSearchState copyWith({
    String? query,
    List<Location>? results,
    List<Location>? history,
    Location? currentLocation,
    Location? reversedLocation,
    bool? loading,
    bool? locating,
    String? error,
    bool clearCurrentLocation = false,
    bool clearReversed = false,
  }) =>
      LocationSearchState(
        query: query ?? this.query,
        results: results ?? this.results,
        history: history ?? this.history,
        currentLocation:
            clearCurrentLocation ? null : (currentLocation ?? this.currentLocation),
        reversedLocation:
            clearReversed ? null : (reversedLocation ?? this.reversedLocation),
        loading: loading ?? this.loading,
        locating: locating ?? this.locating,
        error: error,
      );

  @override
  List<Object?> get props =>
      [query, results, history, currentLocation, reversedLocation, loading, locating, error];
}
