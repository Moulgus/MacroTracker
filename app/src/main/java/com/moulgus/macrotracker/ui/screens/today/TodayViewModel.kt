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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as MacroTrackerApplication

    private val repository = app.repository
    private val userSettingsRepository = app.userSettingsRepository

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val labelFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val selectedDate = MutableStateFlow(TrackingDateUtils.getCurrentTrackingDate())

    // true = użytkownik ogląda "dzisiaj" i ekran ma sam przejść na nowy dzień po 4:00.
    // false = użytkownik ręcznie przegląda starszy dzień, więc go nie przerzucamy.
    private val isFollowingCurrentTrackingDate = MutableStateFlow(true)

    init {
        startTrackingDateWatcher()
    }

    val uiState = selectedDate
        .flatMapLatest { date ->
            val dateText = date.format(isoFormatter)

            combine(
                repository.observeMealsWithEntriesForDate(dateText),
                repository.observeDailyMacroSummary(dateText),
                userSettingsRepository.goalsFlow
            ) { meals: List<MealWithEntries>, summary: DailyMacroSummary, goals: UserMacroGoals ->
                val currentTrackingDate = TrackingDateUtils.getCurrentTrackingDate()

                TodayUiState(
                    date = dateText,
                    dateLabel = date.format(labelFormatter),
                    canMoveToNextDay = date.isBefore(currentTrackingDate),
                    isCurrentTrackingDate = date == currentTrackingDate,
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
                date = TrackingDateUtils.getCurrentTrackingDateString(),
                dateLabel = TrackingDateUtils.getCurrentTrackingDate().format(labelFormatter),
                isLoading = true
            )
        )

    fun moveSelectedDateBack() {
        selectedDate.value = selectedDate.value.minusDays(1)
        isFollowingCurrentTrackingDate.value = false
    }

    fun moveSelectedDateForward() {
        val currentTrackingDate = TrackingDateUtils.getCurrentTrackingDate()

        if (selectedDate.value.isBefore(currentTrackingDate)) {
            selectedDate.value = selectedDate.value.plusDays(1)
        }

        isFollowingCurrentTrackingDate.value = selectedDate.value == currentTrackingDate
    }

    fun selectCurrentTrackingDate() {
        selectedDate.value = TrackingDateUtils.getCurrentTrackingDate()
        isFollowingCurrentTrackingDate.value = true
    }

    fun syncCurrentTrackingDateIfFollowing() {
        if (isFollowingCurrentTrackingDate.value) {
            selectedDate.value = TrackingDateUtils.getCurrentTrackingDate()
        }
    }

    fun deleteMeal(mealID: Long) {
        viewModelScope.launch {
            repository.deleteMealByID(mealID)
            app.refreshWidgets()
        }
    }

    private fun startTrackingDateWatcher() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                syncCurrentTrackingDateIfFollowing()
            }
        }
    }
}