package com.moulgus.macrotracker.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.model.DailyMacroSummary
import com.moulgus.macrotracker.data.local.model.MealWithEntries
import com.moulgus.macrotracker.data.settings.UserMacroGoals
import com.moulgus.macrotracker.util.TrackingDateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                repository.observeMealsWithEntriesForDate(date),
                repository.observeDailyMacroSummary(date),
                userSettingsRepository.goalsFlow
            ) { meals: List<MealWithEntries>, summary: DailyMacroSummary, goals: UserMacroGoals ->
                TodayUiState(
                    date = date,
                    meals = meals,
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

    fun deleteMeal(mealID: Long) {
        viewModelScope.launch {
            repository.deleteMealByID(mealID)
        }
    }

    private fun getTodayDate(): String {
        return TrackingDateUtils.getCurrentTrackingDateString()
    }
}