import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../domain/entities/route_map.dart';
import '../../../domain/entities/route_plan.dart';
import '../../../domain/repositories/local_repository.dart';

part 'home_event.dart';
part 'home_state.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  final LocalRepository _local;

  HomeBloc(this._local) : super(const HomeState()) {
    on<LoadHome>(_onLoad);
    on<DeleteRoute>(_onDeleteRoute);
    on<DeletePlan>(_onDeletePlan);
    on<SetPlanFilter>(_onSetFilter);
  }

  Future<void> _onLoad(LoadHome e, Emitter<HomeState> emit) async {
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

  Future<void> _onDeleteRoute(DeleteRoute e, Emitter<HomeState> emit) async {
    await _local.deleteRoute(e.id);
    add(LoadHome());
  }

  Future<void> _onDeletePlan(DeletePlan e, Emitter<HomeState> emit) async {
    await _local.deletePlanFromDb(e.id);
    add(LoadHome());
  }

  void _onSetFilter(SetPlanFilter e, Emitter<HomeState> emit) {
    emit(state.copyWith(planFilter: e.filter));
  }
}
