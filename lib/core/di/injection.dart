import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../data/datasources/location_datasource.dart';
import '../../data/datasources/osm_datasource.dart';
import '../../data/local/app_database.dart';
import '../../data/repos/export_repo_impl.dart';
import '../../data/repos/local_repo_impl.dart';
import '../../data/repos/location_repo_impl.dart';
import '../../data/repos/plan_repo_impl.dart';
import '../../data/repos/route_repo_impl.dart';
import '../../domain/repos/export_repo.dart';
import '../../domain/repos/local_repo.dart';
import '../../domain/repos/location_repo.dart';
import '../../domain/repos/plan_repository.dart';
import '../../domain/repos/route_repository.dart';
import '../../presentation/bloc/home/home_bloc.dart';
import '../../presentation/bloc/location_search/location_search_bloc.dart';
import '../../presentation/bloc/route_builder/route_session_cubit.dart';
import '../../domain/usecases/suggest_waypoints.dart';
import '../../domain/usecases/build_plan.dart';
import '../../domain/usecases/build_route.dart';
import '../../domain/usecases/export_plan.dart';
import '../../domain/usecases/get_current_location.dart';
import '../../domain/usecases/search_places.dart';

final sl = GetIt.instance;

Future<void> init() async {
  final prefs = await SharedPreferences.getInstance();
  sl.registerSingleton<SharedPreferences>(prefs);

  final db = await $FroomAppDatabase
      .databaseBuilder('youplot_database.db')
      .build();
  sl.registerSingleton<AppDatabase>(db);

  sl.registerSingleton<Dio>(
    Dio(
      BaseOptions(
        connectTimeout: const Duration(seconds: 20),
        receiveTimeout: const Duration(seconds: 40),
      ),
    )
      ..interceptors.add(
        PrettyDioLogger(
          requestHeader: false,
          requestBody: false,
          responseBody: false,
        ),
      ),
  );

  sl.registerLazySingleton(() => OsmDatasource(sl<Dio>()));
  sl.registerLazySingleton(() => LocationDatasource());

  sl.registerLazySingleton<LocationRepo>(
    () => LocationRepoImpl(
      sl<LocationDatasource>(),
      sl<OsmDatasource>(),
    ),
  );
  sl.registerLazySingleton<RouteRepo>(
    () => RouteRepoImpl(sl<OsmDatasource>()),
  );
  sl.registerLazySingleton<PlanRepo>(
    () => PlanRepoImpl(sl<SharedPreferences>()),
  );
  sl.registerLazySingleton<ExportRepo>(() => ExportRepoImpl());
  sl.registerLazySingleton<LocalRepo>(
    () => LocalRepoImpl(sl<AppDatabase>()),
  );

  sl.registerLazySingleton(() => GetCurrentLocation(sl<LocationRepo>()));
  sl.registerLazySingleton(() => SearchPlaces(sl<LocationRepo>()));
  sl.registerLazySingleton(() => BuildRoute(sl<RouteRepo>()));
  sl.registerLazySingleton(() => SuggestWaypoints(sl<RouteRepo>()));
  sl.registerLazySingleton(() => BuildPlan(sl<PlanRepo>()));
  sl.registerLazySingleton(() => ExportPlan(sl<ExportRepo>()));

  sl.registerFactory(() => RouteSessionCubit(sl<SharedPreferences>()));


  sl.registerFactory(
    () => LocationSearchBloc(
      searchPlaces: sl<SearchPlaces>(),
      getCurrentLocation: sl<GetCurrentLocation>(),
      locationRepo: sl<LocationRepo>(),
      prefs: sl<SharedPreferences>(),
    ),
  );
  sl.registerFactory(() => HomeBloc(sl<LocalRepo>()));
}
