package com.you.plot.core.domain.usecase.route

import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRoutesUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    operator fun invoke(): Flow<List<Route>> = repository.getAllRoutes()
}

class GetRouteByIdUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(id: Long): Route? = repository.getRouteById(id)
}

class SaveRouteUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(route: Route): Long {
        require(route.name.isNotBlank()) { "Route name cannot be empty" }
        require(route.totalDistanceKm > 0) { "Route must have a positive distance" }
        return repository.saveRoute(route)
    }
}

class DeleteRouteUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteRoute(id)
}
