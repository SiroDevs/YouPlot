// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// **************************************************************************
// FroomGenerator
// **************************************************************************

abstract class $AppDatabaseBuilderContract {
  /// Adds migrations to the builder.
  $AppDatabaseBuilderContract addMigrations(List<Migration> migrations);

  /// Adds a database [Callback] to the builder.
  $AppDatabaseBuilderContract addCallback(Callback callback);

  /// Creates the database and initializes it.
  Future<AppDatabase> build();
}

// ignore: avoid_classes_with_only_static_members
class $FroomAppDatabase {
  /// Creates a database builder for a persistent database.
  /// Once a database is built, you should keep a reference to it and re-use it.
  static $AppDatabaseBuilderContract databaseBuilder(String name) =>
      _$AppDatabaseBuilder(name);

  /// Creates a database builder for an in memory database.
  /// Information stored in an in memory database disappears when the process is killed.
  /// Once a database is built, you should keep a reference to it and re-use it.
  static $AppDatabaseBuilderContract inMemoryDatabaseBuilder() =>
      _$AppDatabaseBuilder(null);
}

class _$AppDatabaseBuilder implements $AppDatabaseBuilderContract {
  _$AppDatabaseBuilder(this.name);

  final String? name;

  final List<Migration> _migrations = [];

  Callback? _callback;

  @override
  $AppDatabaseBuilderContract addMigrations(List<Migration> migrations) {
    _migrations.addAll(migrations);
    return this;
  }

  @override
  $AppDatabaseBuilderContract addCallback(Callback callback) {
    _callback = callback;
    return this;
  }

  @override
  Future<AppDatabase> build() async {
    final path = name != null
        ? await sqfliteDatabaseFactory.getDatabasePath(name!)
        : ':memory:';
    final database = _$AppDatabase();
    database.database = await database.open(path, _migrations, _callback);
    return database;
  }
}

class _$AppDatabase extends AppDatabase {
  _$AppDatabase([StreamController<String>? listener]) {
    changeListener = listener ?? StreamController<String>.broadcast();
  }

  RouteDao? _routeDaoInstance;

  PlanDao? _planDaoInstance;

  Future<sqflite.Database> open(
    String path,
    List<Migration> migrations, [
    Callback? callback,
  ]) async {
    final databaseOptions = sqflite.OpenDatabaseOptions(
      version: 1,
      onConfigure: (database) async {
        await database.execute('PRAGMA foreign_keys = ON');
        await callback?.onConfigure?.call(database);
      },
      onOpen: (database) async {
        await callback?.onOpen?.call(database);
      },
      onUpgrade: (database, startVersion, endVersion) async {
        await MigrationAdapter.runMigrations(
          database,
          startVersion,
          endVersion,
          migrations,
        );

        await callback?.onUpgrade?.call(database, startVersion, endVersion);
      },
      onCreate: (database, version) async {
        await database.execute(
          'CREATE TABLE IF NOT EXISTS `saved_routes` (`id` TEXT NOT NULL, `origin` TEXT NOT NULL, `originLat` REAL NOT NULL, `originLng` REAL NOT NULL, `destination` TEXT NOT NULL, `destinationLat` REAL NOT NULL, `destinationLng` REAL NOT NULL, `totalDistance` REAL NOT NULL, `totalAscent` REAL NOT NULL, `totalDescent` REAL NOT NULL, `sport` TEXT NOT NULL, `unit` TEXT NOT NULL, `routeJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY (`id`))',
        );
        await database.execute(
          'CREATE TABLE IF NOT EXISTS `saved_plans` (`id` TEXT NOT NULL, `routeId` TEXT NOT NULL, `origin` TEXT NOT NULL, `destination` TEXT NOT NULL, `totalDistance` REAL NOT NULL, `sport` TEXT NOT NULL, `totalDays` INTEGER NOT NULL, `speed` REAL NOT NULL, `startTime` INTEGER NOT NULL, `planJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY (`id`))',
        );

        await callback?.onCreate?.call(database, version);
      },
    );
    return sqfliteDatabaseFactory.openDatabase(path, options: databaseOptions);
  }

  @override
  RouteDao get routeDao {
    return _routeDaoInstance ??= _$RouteDao(database, changeListener);
  }

  @override
  PlanDao get planDao {
    return _planDaoInstance ??= _$PlanDao(database, changeListener);
  }
}

class _$RouteDao extends RouteDao {
  _$RouteDao(this.database, this.changeListener)
    : _queryAdapter = QueryAdapter(database),
      _routeEntityInsertionAdapter = InsertionAdapter(
        database,
        'saved_routes',
        (RouteEntity item) => <String, Object?>{
          'id': item.id,
          'origin': item.origin,
          'originLat': item.originLat,
          'originLng': item.originLng,
          'destination': item.destination,
          'destinationLat': item.destinationLat,
          'destinationLng': item.destinationLng,
          'totalDistance': item.totalDistance,
          'totalAscent': item.totalAscent,
          'totalDescent': item.totalDescent,
          'sport': item.sport,
          'unit': item.unit,
          'routeJson': item.routeJson,
          'createdAt': item.createdAt,
        },
      );

  final sqflite.DatabaseExecutor database;

  final StreamController<String> changeListener;

  final QueryAdapter _queryAdapter;

  final InsertionAdapter<RouteEntity> _routeEntityInsertionAdapter;

  @override
  Future<List<RouteEntity>> getAll() async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_routes ORDER BY createdAt DESC',
      mapper: (Map<String, Object?> row) => RouteEntity(
        id: row['id'] as String,
        origin: row['origin'] as String,
        originLat: row['originLat'] as double,
        originLng: row['originLng'] as double,
        destination: row['destination'] as String,
        destinationLat: row['destinationLat'] as double,
        destinationLng: row['destinationLng'] as double,
        totalDistance: row['totalDistance'] as double,
        totalAscent: row['totalAscent'] as double,
        totalDescent: row['totalDescent'] as double,
        sport: row['sport'] as String,
        unit: row['unit'] as String,
        routeJson: row['routeJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
    );
  }

  @override
  Future<List<RouteEntity>> getRecent(int limit) async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_routes ORDER BY createdAt DESC LIMIT ?1',
      mapper: (Map<String, Object?> row) => RouteEntity(
        id: row['id'] as String,
        origin: row['origin'] as String,
        originLat: row['originLat'] as double,
        originLng: row['originLng'] as double,
        destination: row['destination'] as String,
        destinationLat: row['destinationLat'] as double,
        destinationLng: row['destinationLng'] as double,
        totalDistance: row['totalDistance'] as double,
        totalAscent: row['totalAscent'] as double,
        totalDescent: row['totalDescent'] as double,
        sport: row['sport'] as String,
        unit: row['unit'] as String,
        routeJson: row['routeJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
      arguments: [limit],
    );
  }

  @override
  Future<RouteEntity?> getById(String id) async {
    return _queryAdapter.query(
      'SELECT * FROM saved_routes WHERE id = ?1',
      mapper: (Map<String, Object?> row) => RouteEntity(
        id: row['id'] as String,
        origin: row['origin'] as String,
        originLat: row['originLat'] as double,
        originLng: row['originLng'] as double,
        destination: row['destination'] as String,
        destinationLat: row['destinationLat'] as double,
        destinationLng: row['destinationLng'] as double,
        totalDistance: row['totalDistance'] as double,
        totalAscent: row['totalAscent'] as double,
        totalDescent: row['totalDescent'] as double,
        sport: row['sport'] as String,
        unit: row['unit'] as String,
        routeJson: row['routeJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
      arguments: [id],
    );
  }

  @override
  Future<void> deleteById(String id) async {
    await _queryAdapter.queryNoReturn(
      'DELETE FROM saved_routes WHERE id = ?1',
      arguments: [id],
    );
  }

  @override
  Future<void> insertRoute(RouteEntity route) async {
    await _routeEntityInsertionAdapter.insert(route, OnConflictStrategy.abort);
  }
}

class _$PlanDao extends PlanDao {
  _$PlanDao(this.database, this.changeListener)
    : _queryAdapter = QueryAdapter(database),
      _planEntityInsertionAdapter = InsertionAdapter(
        database,
        'saved_plans',
        (PlanEntity item) => <String, Object?>{
          'id': item.id,
          'routeId': item.routeId,
          'origin': item.origin,
          'destination': item.destination,
          'totalDistance': item.totalDistance,
          'sport': item.sport,
          'totalDays': item.totalDays,
          'speed': item.speed,
          'startTime': item.startTime,
          'planJson': item.planJson,
          'createdAt': item.createdAt,
        },
      );

  final sqflite.DatabaseExecutor database;

  final StreamController<String> changeListener;

  final QueryAdapter _queryAdapter;

  final InsertionAdapter<PlanEntity> _planEntityInsertionAdapter;

  @override
  Future<List<PlanEntity>> getAll() async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_plans ORDER BY createdAt DESC',
      mapper: (Map<String, Object?> row) => PlanEntity(
        id: row['id'] as String,
        routeId: row['routeId'] as String,
        origin: row['origin'] as String,
        destination: row['destination'] as String,
        totalDistance: row['totalDistance'] as double,
        sport: row['sport'] as String,
        totalDays: row['totalDays'] as int,
        speed: row['speed'] as double,
        startTime: row['startTime'] as int,
        planJson: row['planJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
    );
  }

  @override
  Future<List<PlanEntity>> getRecent(int limit) async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_plans ORDER BY createdAt DESC LIMIT ?1',
      mapper: (Map<String, Object?> row) => PlanEntity(
        id: row['id'] as String,
        routeId: row['routeId'] as String,
        origin: row['origin'] as String,
        destination: row['destination'] as String,
        totalDistance: row['totalDistance'] as double,
        sport: row['sport'] as String,
        totalDays: row['totalDays'] as int,
        speed: row['speed'] as double,
        startTime: row['startTime'] as int,
        planJson: row['planJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
      arguments: [limit],
    );
  }

  @override
  Future<List<PlanEntity>> getUpcoming(int nowMs) async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_plans WHERE startTime > ?1 ORDER BY startTime ASC',
      mapper: (Map<String, Object?> row) => PlanEntity(
        id: row['id'] as String,
        routeId: row['routeId'] as String,
        origin: row['origin'] as String,
        destination: row['destination'] as String,
        totalDistance: row['totalDistance'] as double,
        sport: row['sport'] as String,
        totalDays: row['totalDays'] as int,
        speed: row['speed'] as double,
        startTime: row['startTime'] as int,
        planJson: row['planJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
      arguments: [nowMs],
    );
  }

  @override
  Future<List<PlanEntity>> getPast(int nowMs) async {
    return _queryAdapter.queryList(
      'SELECT * FROM saved_plans WHERE startTime <= ?1 ORDER BY startTime DESC',
      mapper: (Map<String, Object?> row) => PlanEntity(
        id: row['id'] as String,
        routeId: row['routeId'] as String,
        origin: row['origin'] as String,
        destination: row['destination'] as String,
        totalDistance: row['totalDistance'] as double,
        sport: row['sport'] as String,
        totalDays: row['totalDays'] as int,
        speed: row['speed'] as double,
        startTime: row['startTime'] as int,
        planJson: row['planJson'] as String,
        createdAt: row['createdAt'] as int,
      ),
      arguments: [nowMs],
    );
  }

  @override
  Future<void> deleteById(String id) async {
    await _queryAdapter.queryNoReturn(
      'DELETE FROM saved_plans WHERE id = ?1',
      arguments: [id],
    );
  }

  @override
  Future<void> insertPlan(PlanEntity plan) async {
    await _planEntityInsertionAdapter.insert(plan, OnConflictStrategy.abort);
  }
}
