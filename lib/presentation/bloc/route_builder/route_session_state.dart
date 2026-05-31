part of 'route_session_cubit.dart';

class RouteSessionState extends Equatable {
  final AppStep step;
  final SportType sport;
  final DistanceUnit unit;
  final Location? origin;
  final Location? destination;
  final RouteMap? route;
  final RoutePlan? plan;
  final MapController? mapController;

  const RouteSessionState({
    this.step = AppStep.setup,
    this.sport = SportType.cycling,
    this.unit = DistanceUnit.kilometers,
    this.origin,
    this.destination,
    this.route,
    this.plan,
    this.mapController,
  });

  bool get canProceedFromSetup => origin != null && destination != null;

  RouteSessionState copyWith({
    AppStep? step,
    SportType? sport,
    DistanceUnit? unit,
    Location? origin,
    Location? destination,
    RouteMap? route,
    RoutePlan? plan,
    MapController? mapController,
  }) {
    return RouteSessionState(
      step: step ?? this.step,
      sport: sport ?? this.sport,
      unit: unit ?? this.unit,
      origin: origin ?? this.origin,
      destination: destination ?? this.destination,
      route: route ?? this.route,
      plan: plan ?? this.plan,
      mapController: mapController ?? this.mapController,
    );
  }

  @override
  List<Object?> get props => [
    step,
    sport,
    unit,
    origin,
    destination,
    route,
    plan,
  ];
}
