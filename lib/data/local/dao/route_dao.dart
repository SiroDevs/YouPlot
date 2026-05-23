import 'package:froom/froom.dart';

import '../entities/route_entity.dart';

@dao
abstract class RouteDao {
  @Query('SELECT * FROM saved_routes ORDER BY createdAt DESC')
  Future<List<RouteEntity>> getAll();

  @Query('SELECT * FROM saved_routes ORDER BY createdAt DESC LIMIT :limit')
  Future<List<RouteEntity>> getRecent(int limit);

  @Query('SELECT * FROM saved_routes WHERE id = :id')
  Future<RouteEntity?> getById(String id);

  @insert
  Future<void> insertRoute(RouteEntity route);

  @Query('DELETE FROM saved_routes WHERE id = :id')
  Future<void> deleteById(String id);
}
