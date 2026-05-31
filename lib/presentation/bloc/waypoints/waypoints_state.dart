part of 'waypoints_cubit.dart';

class WaypointsState extends Equatable {
  final List<Location> viaPoints;
  final List<Waypoint> suggestions;
  final bool usingSuggestions;
  final bool loading;
  final String? error;

  const WaypointsState({
    this.viaPoints = const [],
    this.suggestions = const [],
    this.usingSuggestions = false,
    this.loading = false,
    this.error,
  });

  WaypointsState copyWith({
    List<Location>? viaPoints,
    List<Waypoint>? suggestions,
    bool? usingSuggestions,
    bool? loading,
    String? error,
    bool clearError = false,
  }) {
    return WaypointsState(
      viaPoints: viaPoints ?? this.viaPoints,
      suggestions: suggestions ?? this.suggestions,
      usingSuggestions: usingSuggestions ?? this.usingSuggestions,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
    );
  }

  @override
  List<Object?> get props =>
      [viaPoints, suggestions, usingSuggestions, loading, error];
}
