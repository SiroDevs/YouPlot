part of 'route_builder_bloc.dart';

enum AppStep {
  setup,       // sport, units, origin, destination
  waypoints,   // suggest vs custom
  generating,  // loading spinner while API runs
  map,         // map + elevation
  plan,        // days, speed, breaks
  review,      // daily segments
  export,      // export options
}

class RouteBuilderState extends Equatable {
  final AppStep step;
  final SportType sport;
  final DistanceUnit unit;
  final Location? origin;
  final Location? destination;

  final List<Location> viaPoints;
  final List<Waypoint> suggestions;
  final bool usingSuggestions;

  final RouteMap? route;

  final int days;
  final double speedKmh;
  final DateTime startTime;
  final Set<BreakType> selectedBreaks;

  final RoutePlan? plan;

  final bool loading;
  final String? error;
  final String? exportedPath;

  /// The live Mapbox controller — non-null once the map widget reports ready.
  /// Not included in props so state changes here don't trigger unnecessary
  /// widget rebuilds.
  final MapboxMap? mapController;

  const RouteBuilderState({
    this.step = AppStep.setup,
    this.sport = SportType.hiking,
    this.unit = DistanceUnit.kilometers,
    this.origin,
    this.destination,
    this.viaPoints = const [],
    this.suggestions = const [],
    this.usingSuggestions = false,
    this.route,
    this.days = 1,
    this.speedKmh = 4.0,
    required this.startTime,
    this.selectedBreaks = const {},
    this.plan,
    this.loading = false,
    this.error,
    this.exportedPath,
    this.mapController,
  });

  bool get canProceed => origin != null && destination != null;

  double get displaySpeed =>
      unit == DistanceUnit.miles ? speedKmh * 0.621371 : speedKmh;

  RouteBuilderState copyWith({
    AppStep? step,
    SportType? sport,
    DistanceUnit? unit,
    Location? origin,
    Location? destination,
    List<Location>? viaPoints,
    List<Waypoint>? suggestions,
    bool? usingSuggestions,
    RouteMap? route,
    int? days,
    double? speedKmh,
    DateTime? startTime,
    Set<BreakType>? selectedBreaks,
    RoutePlan? plan,
    bool? loading,
    String? error,
    String? exportedPath,
    bool clearError = false,
    bool clearExport = false,
    bool clearOrigin = false,
    bool clearDestination = false,
    MapboxMap? mapController,
  }) {
    return RouteBuilderState(
      step: step ?? this.step,
      sport: sport ?? this.sport,
      unit: unit ?? this.unit,
      origin: clearOrigin ? null : (origin ?? this.origin),
      destination: clearDestination ? null : (destination ?? this.destination),
      viaPoints: viaPoints ?? this.viaPoints,
      suggestions: suggestions ?? this.suggestions,
      usingSuggestions: usingSuggestions ?? this.usingSuggestions,
      route: route ?? this.route,
      days: days ?? this.days,
      speedKmh: speedKmh ?? this.speedKmh,
      startTime: startTime ?? this.startTime,
      selectedBreaks: selectedBreaks ?? this.selectedBreaks,
      plan: plan ?? this.plan,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
      exportedPath: clearExport ? null : (exportedPath ?? this.exportedPath),
      mapController: mapController ?? this.mapController,
    );
  }

  @override
  List<Object?> get props => [
        step, sport, unit, origin, destination,
        viaPoints, suggestions, usingSuggestions,
        route, days, speedKmh, startTime, selectedBreaks,
        plan, loading, error, exportedPath,
        // mapController intentionally excluded — it's a mutable object and
        // including it would cause rebuild loops.
      ];
}
