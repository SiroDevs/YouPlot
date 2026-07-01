package com.you.plot.feature.route.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.GetRouteByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteDetailUiState(
    val route: Route? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val deleteError: String? = null,
)

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
) : ViewModel() {

    private val routeId: Long = checkNotNull(savedStateHandle["routeId"])

    private val _state = MutableStateFlow(RouteDetailUiState())
    val state: StateFlow<RouteDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val route = getRouteByIdUseCase(routeId)
            _state.update { it.copy(route = route, isLoading = false) }
        }
    }

    fun deleteRoute() {
        viewModelScope.launch {
            deleteRouteUseCase(routeId).fold(
                onSuccess = { _state.update { it.copy(isDeleted = true) } },
                onFailure = { e ->
                    _state.update { it.copy(deleteError = e.message ?: "Cannot delete route") }
                },
            )
        }
    }

    fun clearDeleteError() = _state.update { it.copy(deleteError = null) }
}
