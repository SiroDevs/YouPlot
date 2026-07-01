package com.you.plot.core.domain.usecase.startpoint

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.repos.StartPointRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllStartPointsUseCase @Inject constructor(private val repo: StartPointRepo) {
    operator fun invoke(): Flow<List<StartPoint>> = repo.getAll()
}

class GetFavoriteStartPointsUseCase @Inject constructor(private val repo: StartPointRepo) {
    operator fun invoke(): Flow<List<StartPoint>> = repo.getFavorites()
}

class GetTrashedStartPointsUseCase @Inject constructor(private val repo: StartPointRepo) {
    operator fun invoke(): Flow<List<StartPoint>> = repo.getTrashed()
}

class GetStartPointByIdUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(id: Long): StartPoint? = repo.getById(id)
}

class SaveStartPointUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(sp: StartPoint): Long {
        require(sp.name.isNotBlank()) { "Start point name cannot be empty" }
        return if (sp.id == 0L) repo.save(sp) else {
            repo.update(sp); sp.id
        }
    }
}

/**
 * Called from the plotter every time a route is saved so that frequently-used starting
 * spots bubble to the top of the start-point list.
 */
class RecordStartPointUsageUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(name: String, position: LatLng, countryCode: String): Long =
        repo.recordUsage(name, position, countryCode)
}

class SetStartPointFavoriteUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(id: Long, favorite: Boolean) = repo.setFavorite(id, favorite)
}

class DeleteStartPointUseCase @Inject constructor(private val repo: StartPointRepo) {
    /** Soft-deletes; the trash bin can restore it. */
    suspend operator fun invoke(id: Long) = repo.softDelete(id)
}

class RestoreStartPointUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(id: Long) = repo.restore(id)
}

class PermanentlyDeleteStartPointUseCase @Inject constructor(private val repo: StartPointRepo) {
    suspend operator fun invoke(id: Long) = repo.deletePermanently(id)
}
