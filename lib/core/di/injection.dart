import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../data/datasources/location_datasource.dart';
import '../../data/datasources/osm_datasource.dart';
import '../../data/local/app_database.dart';
import '../../data/repositories/export_repository_impl.dart';
import '../../data/repositories/local_repository_impl.dart';
import '../../data/repositories/location_repository_impl.dart';
import '../../data/repositories/plan_repository_impl.dart';
import '../../data/repositories/route_repository_impl.dart';
import '../../domain/repositories/export_repository.dart';
import '../../domain/repositories/local_repository.dart';
import '../../domain/repositories/location_repository.dart';
import '../../domain/repositories/plan_repository.dart';
import '../../domain/repositories/route_repository.dart';
import '../../presentation/bloc/home/home_bloc.dart';
import '../../presentation/bloc/location_search/location_search_bloc.dart';
import '../../presentation/bloc/route_builder/route_builder_bloc.dart';
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

  // ── Datasources ────────────────────────────────────────────────────────────
  sl.registerLazySingleton(() => OsmDatasource(sl<Dio>()));
  sl.registerLazySingleton(() => LocationDatasource());

  // ── Repositories ───────────────────────────────────────────────────────────
  sl.registerLazySingleton<LocationRepository>(
    () => LocationRepositoryImpl(
      sl<LocationDatasource>(),
      sl<OsmDatasource>(),
    ),
  );
  sl.registerLazySingleton<RouteRepository>(
    () => RouteRepositoryImpl(sl<OsmDatasource>()),
  );
  sl.registerLazySingleton<PlanRepository>(
    () => PlanRepositoryImpl(sl<SharedPreferences>()),
  );
  sl.registerLazySingleton<ExportRepository>(() => ExportRepositoryImpl());
  sl.registerLazySingleton<LocalRepository>(
    () => LocalRepositoryImpl(sl<AppDatabase>()),
  );

  // ── Usecases ───────────────────────────────────────────────────────────────
  sl.registerLazySingleton(() => GetCurrentLocation(sl<LocationRepository>()));
  sl.registerLazySingleton(() => SearchPlaces(sl<LocationRepository>()));
  sl.registerLazySingleton(() => BuildRoute(sl<RouteRepository>()));
  sl.registerLazySingleton(() => SuggestWaypoints(sl<RouteRepository>()));
  sl.registerLazySingleton(() => BuildPlan(sl<PlanRepository>()));
  sl.registerLazySingleton(() => ExportPlan(sl<ExportRepository>()));

  // ── Blocs ──────────────────────────────────────────────────────────────────
  sl.registerFactory(
    () => RouteBuilderBloc(
      buildRoute: sl<BuildRoute>(),
      suggestWaypoints: sl<SuggestWaypoints>(),
      buildPlan: sl<BuildPlan>(),
      exportPlan: sl<ExportPlan>(),
      local: sl<LocalRepository>(),
      prefs: sl<SharedPreferences>(),
    ),
  );
  sl.registerFactory(
    () => LocationSearchBloc(
      searchPlaces: sl<SearchPlaces>(),
      getCurrentLocation: sl<GetCurrentLocation>(),
      locationRepo: sl<LocationRepository>(),
      prefs: sl<SharedPreferences>(),
    ),
  );
  sl.registerFactory(() => HomeBloc(sl<LocalRepository>()));
}
