part of 'home_bloc.dart';

class HomeState extends Equatable {
  final bool loading;
  final List<RouteMap> routes;
  final List<RoutePlan> allPlans;
  final List<RoutePlan> upcomingPlans;
  final List<RoutePlan> pastPlans;
  final PlanFilter planFilter;

  const HomeState({
    this.loading = false,
    this.routes = const [],
    this.allPlans = const [],
    this.upcomingPlans = const [],
    this.pastPlans = const [],
    this.planFilter = PlanFilter.all,
  });

  List<RoutePlan> get activePlans {
    switch (planFilter) {
      case PlanFilter.all:
        return allPlans;
      case PlanFilter.upcoming:
        return upcomingPlans;
      case PlanFilter.past:
        return pastPlans;
    }
  }

  bool get isEmpty => routes.isEmpty && allPlans.isEmpty;

  HomeState copyWith({
    bool? loading,
    List<RouteMap>? routes,
    List<RoutePlan>? allPlans,
    List<RoutePlan>? upcomingPlans,
    List<RoutePlan>? pastPlans,
    PlanFilter? planFilter,
  }) {
    return HomeState(
      loading: loading ?? this.loading,
      routes: routes ?? this.routes,
      allPlans: allPlans ?? this.allPlans,
      upcomingPlans: upcomingPlans ?? this.upcomingPlans,
      pastPlans: pastPlans ?? this.pastPlans,
      planFilter: planFilter ?? this.planFilter,
    );
  }

  @override
  List<Object?> get props =>
      [loading, routes, allPlans, upcomingPlans, pastPlans, planFilter];
}
