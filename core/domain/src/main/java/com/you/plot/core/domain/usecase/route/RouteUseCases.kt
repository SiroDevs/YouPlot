package com.you.plot.core.domain.usecase.route

import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.repos.RouteRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRoutesUseCase @Inject constructor(
    private val repository: RouteRepo
) {
    operator fun invoke(): Flow<List<Route>> = repository.getAllRoutes()
}

class GetRouteByIdUseCase @Inject constructor(
    private val repository: RouteRepo
) {
    suspend operator fun invoke(id: Long): Route? = repository.getRouteById(id)
}

class SaveRouteUseCase @Inject constructor(
    private val repository: RouteRepo
) {
    suspend operator fun invoke(route: Route): Long {
        require(route.name.isNotBlank()) { "Route name cannot be empty" }
        require(route.totalDist > 0) { "Route must have a positive distance" }
        return repository.saveRoute(route)
    }
}

class UpdateRouteUseCase @Inject constructor(
    private val repository: RouteRepo,
) {
    suspend operator fun invoke(route: Route) {
        require(route.id > 0L) { "Cannot update an unsaved route" }
        require(route.name.isNotBlank()) { "Route name cannot be empty" }
        repository.updateRoute(route)
    }
}

class DeleteRouteUseCase @Inject constructor(
    private val repository: RouteRepo
) {
    /**
     * Soft-deletes a route, moving it to the trash bin. Fails if the route still
     * has active (non-trashed) plans attached — the caller must clear those first.
     */
    suspend operator fun invoke(id: Long): Result<Unit> {
        val active = repository.countActivePlansForRoute(id)
        if (active > 0) return Result.failure(RouteHasActivePlansException(active))
        repository.softDeleteRoute(id)
        return Result.success(Unit)
    }
}

class RouteHasActivePlansException(val planCount: Int) : Exception(
    "Route has $planCount active plan${if (planCount == 1) "" else "s"} attached. Delete the plans first."
)

class GetTrashedRoutesUseCase @Inject constructor(private val repository: RouteRepo) {
    operator fun invoke() = repository.getTrashedRoutes()
}

class GetFavoriteRoutesUseCase @Inject constructor(private val repository: RouteRepo) {
    operator fun invoke() = repository.getFavoriteRoutes()
}

class SetRouteFavoriteUseCase @Inject constructor(private val repository: RouteRepo) {
    suspend operator fun invoke(id: Long, favorite: Boolean) =
        repository.setRouteFavorite(id, favorite)
}

class RestoreRouteUseCase @Inject constructor(private val repository: RouteRepo) {
    suspend operator fun invoke(id: Long) = repository.restoreRoute(id)
}

class PermanentlyDeleteRouteUseCase @Inject constructor(private val repository: RouteRepo) {
    suspend operator fun invoke(id: Long) = repository.deleteRoute(id)
}
