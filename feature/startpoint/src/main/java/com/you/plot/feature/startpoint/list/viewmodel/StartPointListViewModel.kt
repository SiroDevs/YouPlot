package com.you.plot.feature.startpoint.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.usecase.startpoint.DeleteStartPointUseCase
import com.you.plot.core.domain.usecase.startpoint.GetAllStartPointsUseCase
import com.you.plot.core.domain.usecase.startpoint.GetFavoriteStartPointsUseCase
import com.you.plot.core.domain.usecase.startpoint.SetStartPointFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StartPointListTab { RECENT, FAVORITES }

data class StartPointListUiState(
    val recent: List<StartPoint> = emptyList(),
    val favorites: List<StartPoint> = emptyList(),
    val selectedTab: StartPointListTab = StartPointListTab.RECENT,
    val isLoading: Boolean = true,
    val menuTargetId: Long? = null,
)

@HiltViewModel
class StartPointListViewModel @Inject constructor(
    getAll: GetAllStartPointsUseCase,
    getFavorites: GetFavoriteStartPointsUseCase,
    private val setFavorite: SetStartPointFavoriteUseCase,
    private val deleteStartPoint: DeleteStartPointUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StartPointListUiState())
    val state: StateFlow<StartPointListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAll().collect { list ->
                _state.update { it.copy(recent = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getFavorites().collect { list -> _state.update { it.copy(favorites = list) } }
        }
    }

    fun selectTab(tab: StartPointListTab) = _state.update { it.copy(selectedTab = tab) }

    fun openMenu(id: Long) = _state.update { it.copy(menuTargetId = id) }
    fun dismissMenu() = _state.update { it.copy(menuTargetId = null) }

    fun toggleFavorite(sp: StartPoint) = viewModelScope.launch {
        setFavorite(sp.id, !sp.isFavorite)
    }

    fun delete(id: Long) = viewModelScope.launch {
        deleteStartPoint(id)
        _state.update { it.copy(menuTargetId = null) }
    }
}
