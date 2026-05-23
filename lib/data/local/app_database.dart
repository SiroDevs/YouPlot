// dart run build_runner build --delete-conflicting-outputs
// to regenerate app_database.g.dart after any entity / DAO changes.

import 'dart:async';

import 'package:froom/froom.dart';

import 'dao/plan_dao.dart';
import 'dao/route_dao.dart';
import 'entities/plan_entity.dart';
import 'entities/route_entity.dart';

part 'app_database.g.dart';

@Database(entities: [RouteEntity, PlanEntity], version: 1)
abstract class AppDatabase extends FroomDatabase {
  RouteDao get routeDao;
  PlanDao get planDao;
}
