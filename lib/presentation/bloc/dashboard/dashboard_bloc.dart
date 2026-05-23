import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';
import '../../../domain/repositories/local_repository.dart';

part 'dashboard_event.dart';
part 'dashboard_state.dart';

class DashboardBloc extends Bloc<DashboardEvent, DashboardState> {
  final LocalRepository _local;

  DashboardBloc(this._local) : super(const DashboardState()) {
    on<LoadDashboard>(_onLoad);
    on<DeleteRoute>(_onDeleteRoute);
    on<DeletePlan>(_onDeletePlan);
    on<SetPlanFilter>(_onSetFilter);
  }

  Future<void> _onLoad(LoadDashboard e, Emitter<DashboardState> emit) async {
    emit(state.copyWith(loading: true));

    final routesResult = await _local.loadRoutes(limit: 10);
    final plansResult = await _local.loadPlansFromDb(limit: 20);
    final upcomingResult = await _local.loadUpcomingPlans();
    final pastResult = await _local.loadPastPlans();

    final routes = routesResult.fold((_) => <RouteMap>[], (r) => r);
    final allPlans = plansResult.fold((_) => <RoutePlan>[], (r) => r);
    final upcoming = upcomingResult.fold((_) => <RoutePlan>[], (r) => r);
    final past = pastResult.fold((_) => <RoutePlan>[], (r) => r);

    emit(state.copyWith(
      loading: false,
      routes: routes,
      allPlans: allPlans,
      upcomingPlans: upcoming,
      pastPlans: past,
    ));
  }

  Future<void> _onDeleteRoute(DeleteRoute e, Emitter<DashboardState> emit) async {
    await _local.deleteRoute(e.id);
    add(LoadDashboard());
  }

  Future<void> _onDeletePlan(DeletePlan e, Emitter<DashboardState> emit) async {
    await _local.deletePlanFromDb(e.id);
    add(LoadDashboard());
  }

  void _onSetFilter(SetPlanFilter e, Emitter<DashboardState> emit) {
    emit(state.copyWith(planFilter: e.filter));
  }
}
