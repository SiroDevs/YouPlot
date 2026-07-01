package com.you.plot.core.data.impls

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.database.converter.toDomain
import com.you.plot.core.database.converter.toEntity
import com.you.plot.core.database.daos.StartPointDao
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.repos.StartPointRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StartPointRepoImpl @Inject constructor(
    private val dao: StartPointDao,
) : StartPointRepo {
    override fun getAll(): Flow<List<StartPoint>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<StartPoint>> =
        dao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun getTrashed(): Flow<List<StartPoint>> =
        dao.getTrashed().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): StartPoint? = dao.getById(id)?.toDomain()

    override suspend fun recordUsage(name: String, position: LatLng, countryCode: String): Long {
        val now = System.currentTimeMillis()
        val existing = dao.findNearby(position.latitude, position.longitude)
        return if (existing != null) {
            dao.incrementUsage(existing.id, now)
            existing.id
        } else {
            dao.insert(
                StartPoint(
                    name = name,
                    position = position,
                    countryCode = countryCode,
                    usageCount = 1,
                    lastUsedAt = now,
                ).toEntity()
            )
        }
    }

    override suspend fun save(startPoint: StartPoint): Long =
        dao.insert(startPoint.toEntity())

    override suspend fun update(startPoint: StartPoint) = dao.update(startPoint.toEntity())

    override suspend fun incrementUsage(id: Long) =
        dao.incrementUsage(id, System.currentTimeMillis())

    override suspend fun softDelete(id: Long) =
        dao.softDelete(id, System.currentTimeMillis())

    override suspend fun restore(id: Long) = dao.restore(id)

    override suspend fun setFavorite(id: Long, favorite: Boolean) =
        dao.setFavorite(id, favorite)

    override suspend fun deletePermanently(id: Long) = dao.deletePermanently(id)

    override suspend fun purgeExpired(cutoff: Long) = dao.purgeExpired(cutoff)
}
