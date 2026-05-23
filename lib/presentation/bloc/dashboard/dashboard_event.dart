part of 'dashboard_bloc.dart';

sealed class DashboardEvent {}

class LoadDashboard extends DashboardEvent {}

class DeleteRoute extends DashboardEvent {
  final String id;
  DeleteRoute(this.id);
}

class DeletePlan extends DashboardEvent {
  final String id;
  DeletePlan(this.id);
}

enum PlanFilter { all, upcoming, past }

class SetPlanFilter extends DashboardEvent {
  final PlanFilter filter;
  SetPlanFilter(this.filter);
}
