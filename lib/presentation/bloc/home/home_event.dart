part of 'home_bloc.dart';

sealed class HomeEvent {}

class LoadHome extends HomeEvent {}

class DeleteRoute extends HomeEvent {
  final String id;
  DeleteRoute(this.id);
}

class DeletePlan extends HomeEvent {
  final String id;
  DeletePlan(this.id);
}

enum PlanFilter { all, upcoming, past }

class SetPlanFilter extends HomeEvent {
  final PlanFilter filter;
  SetPlanFilter(this.filter);
}
