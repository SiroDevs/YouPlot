part of 'location_search_bloc.dart';

class LocationSearchState extends Equatable {
  final String query;
  final List<Location> results;
  final Location? currentLocation;
  final bool loading;
  final bool locating;
  final String? error;

  const LocationSearchState({
    this.query = '',
    this.results = const [],
    this.currentLocation,
    this.loading = false,
    this.locating = false,
    this.error,
  });

  LocationSearchState copyWith({
    String? query,
    List<Location>? results,
    Location? currentLocation,
    bool? loading,
    bool? locating,
    String? error,
  }) =>
      LocationSearchState(
        query: query ?? this.query,
        results: results ?? this.results,
        currentLocation: currentLocation ?? this.currentLocation,
        loading: loading ?? this.loading,
        locating: locating ?? this.locating,
        error: error,
      );

  @override
  List<Object?> get props =>
      [query, results, currentLocation, loading, locating, error];
}
