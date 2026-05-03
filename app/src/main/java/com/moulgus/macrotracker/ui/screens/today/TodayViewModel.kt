package com.moulgus.macrotracker.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.model.DailyMacroSummary
import com.moulgus.macrotracker.data.settings.UserMacroGoals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.moulgus.macrotracker.util.TrackingDateUtils

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as MacroTrackerApplication

    private val repository = app.repository
    private val userSettingsRepository = app.userSettingsRepository

    private val selectedDate = MutableStateFlow(getTodayDate())

    val uiState = selectedDate
        .flatMapLatest { date ->
            combine(
                repository.observeEntriesForDate(date),
                repository.observeDailyMacroSummary(date),
                userSettingsRepository.goalsFlow
            ) { entries: List<MealEntryEntity>, summary: DailyMacroSummary, goals: UserMacroGoals ->
                TodayUiState(
                    date = date,
                    entries = entries,
                    eatenKcal = summary.kcal,
                    eatenProtein = summary.protein,
                    eatenCarbs = summary.carbs,
                    eatenFat = summary.fat,
                    kcalGoal = goals.kcalGoal,
                    proteinGoal = goals.proteinGoal,
                    carbsGoal = goals.carbsGoal,
                    fatGoal = goals.fatGoal,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(
                date = getTodayDate(),
                isLoading = true
            )
        )

    fun refreshToday() {
        selectedDate.value = getTodayDate()
    }

    fun deleteEntry(entry: MealEntryEntity) {
        viewModelScope.launch {
            repository.deleteMealEntry(entry)
        }
    }

    fun deleteEntryByID(entryID: Long) {
        viewModelScope.launch {
            repository.deleteMealEntryByID(entryID)
        }
    }

    private fun getTodayDate(): String {
        return TrackingDateUtils.getCurrentTrackingDateString()
    }
}