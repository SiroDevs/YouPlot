import 'package:froom/froom.dart';

import '../entities/plan_entity.dart';

@dao
abstract class PlanDao {
  @Query('SELECT * FROM plans ORDER BY createdAt DESC')
  Future<List<PlanEntity>> getAll();

  @Query('SELECT * FROM plans ORDER BY createdAt DESC LIMIT :limit')
  Future<List<PlanEntity>> getRecent(int limit);

  @Query(
    'SELECT * FROM plans WHERE startTimeMs > :nowMs ORDER BY startTimeMs ASC',
  )
  Future<List<PlanEntity>> getUpcoming(int nowMs);

  @Query(
    'SELECT * FROM plans WHERE startTimeMs <= :nowMs ORDER BY startTimeMs DESC',
  )
  Future<List<PlanEntity>> getPast(int nowMs);

  @insert
  Future<void> insertPlan(PlanEntity plan);

  @Query('DELETE FROM plans WHERE id = :id')
  Future<void> deleteById(String id);
}
