package com.you.plot.feature.plan.details.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.usecase.plan.GetPlanByIdUseCase
import com.you.plot.feature.plan.details.utils.PlanDetailUiState
import com.you.plot.feature.plan.details.utils.ReminderEntry
import com.you.plot.feature.plan.reminder.PlanReminder
import com.you.plot.feature.plan.reminder.cancelReminder
import com.you.plot.feature.plan.reminder.reminderTag
import com.you.plot.feature.plan.reminder.scheduleReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlanByIdUseCase: GetPlanByIdUseCase,
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _state = MutableStateFlow(PlanDetailUiState())
    val state: StateFlow<PlanDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val plan = getPlanByIdUseCase(planId)
            _state.update { it.copy(plan = plan, isLoading = false) }
        }
    }

    fun selectDay(day: Int) = _state.update { it.copy(selectedDay = day) }

    // ── Reminders ─────────────────────────────────────────────────────────────

    fun showAddReminderDialog() = _state.update { it.copy(showAddReminderDialog = true) }
    fun hideAddReminderDialog() = _state.update { it.copy(showAddReminderDialog = false) }

    fun addReminder(context: Context, label: String, fireAtMillis: Long) {
        val plan = _state.value.plan ?: return
        val index = _state.value.reminders.size
        val tag = reminderTag(plan.id, index)
        val entry = ReminderEntry(index = index, label = label, fireAtMillis = fireAtMillis)
        scheduleReminder(
            context,
            PlanReminder(
                tag = tag,
                title = "YouPlot – ${plan.name}",
                message = label,
                fireAtMillis = fireAtMillis,
            ),
        )
        _state.update { it.copy(reminders = it.reminders + entry, showAddReminderDialog = false) }
    }

    fun removeReminder(context: Context, entry: ReminderEntry) {
        val plan = _state.value.plan ?: return
        cancelReminder(context, reminderTag(plan.id, entry.index))
        _state.update { it.copy(reminders = it.reminders.filter { r -> r.index != entry.index }) }
    }
}
