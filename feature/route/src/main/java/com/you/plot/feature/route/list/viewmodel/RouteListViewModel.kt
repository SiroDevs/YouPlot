package com.you.plot.feature.route.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.GetAllRoutesUseCase
import com.you.plot.core.domain.usecase.route.GetFavoriteRoutesUseCase
import com.you.plot.core.domain.usecase.route.SetRouteFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RouteListTab { RECENT, LISTS, FAVORITES }

data class RouteListUiState(
    val recent: List<Route> = emptyList(),
    val favorites: List<Route> = emptyList(),
    val selectedTab: RouteListTab = RouteListTab.RECENT,
    val isLoading: Boolean = true,
    val menuTargetId: Long? = null,
    val deleteError: String? = null,
)

@HiltViewModel
class RouteListViewModel @Inject constructor(
    getAllRoutes: GetAllRoutesUseCase,
    getFavoriteRoutes: GetFavoriteRoutesUseCase,
    private val setFavorite: SetRouteFavoriteUseCase,
    private val deleteRoute: DeleteRouteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RouteListUiState())
    val uiState: StateFlow<RouteListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllRoutes().collect { list ->
                _state.update { it.copy(recent = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getFavoriteRoutes().collect { list -> _state.update { it.copy(favorites = list) } }
        }
    }

    fun selectTab(tab: RouteListTab) = _state.update { it.copy(selectedTab = tab) }
    fun openMenu(id: Long) = _state.update { it.copy(menuTargetId = id) }
    fun dismissMenu() = _state.update { it.copy(menuTargetId = null) }

    fun toggleFavorite(route: Route) = viewModelScope.launch {
        setFavorite(route.id, !route.isFavorite)
    }

    fun delete(id: Long) = viewModelScope.launch {
        deleteRoute(id).fold(
            onSuccess = { _state.update { it.copy(menuTargetId = null) } },
            onFailure = { e ->
                _state.update {
                    it.copy(menuTargetId = null, deleteError = e.message ?: "Cannot delete")
                }
            },
        )
    }

    fun clearDeleteError() = _state.update { it.copy(deleteError = null) }
}
