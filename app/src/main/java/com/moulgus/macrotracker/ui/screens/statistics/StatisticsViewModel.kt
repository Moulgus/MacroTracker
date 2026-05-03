package com.moulgus.macrotracker.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.settings.UserMacroGoals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.moulgus.macrotracker.util.TrackingDateUtils

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as MacroTrackerApplication

    private val repository = app.repository
    private val userSettingsRepository = app.userSettingsRepository

    private val selectedDays = MutableStateFlow(7)
    private val selectedMetric = MutableStateFlow(StatisticsMetric.KCAL)

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val labelFormatter = DateTimeFormatter.ofPattern("dd.MM")

    private val entriesFlow = selectedDays.flatMapLatest { days ->
        val today = TrackingDateUtils.getCurrentTrackingDate()
        val startDate = today.minusDays(days.toLong() - 1L)

        repository.observeEntriesBetweenDates(
            startDate = startDate.format(isoFormatter),
            endDate = today.format(isoFormatter)
        )
    }

    val uiState = combine(
        selectedDays,
        selectedMetric,
        entriesFlow,
        userSettingsRepository.goalsFlow
    ) { days: Int, metric: StatisticsMetric, entries: List<MealEntryEntity>, goals: UserMacroGoals ->

        val today = TrackingDateUtils.getCurrentTrackingDate()
        val startDate = today.minusDays(days.toLong() - 1L)

        val dayItems = buildList {
            for (dayOffset in 0 until days) {
                val date = startDate.plusDays(dayOffset.toLong())
                val dateText = date.format(isoFormatter)

                val entriesForDay = entries.filter { it.date == dateText }

                add(
                    StatisticsDayItem(
                        date = dateText,
                        label = date.format(labelFormatter),
                        kcal = entriesForDay.sumOf { it.kcal },
                        protein = entriesForDay.sumOf { it.protein },
                        carbs = entriesForDay.sumOf { it.carbs },
                        fat = entriesForDay.sumOf { it.fat }
                    )
                )
            }
        }

        StatisticsUiState(
            selectedDays = days,
            selectedMetric = metric,
            days = dayItems,
            kcalGoal = goals.kcalGoal,
            proteinGoal = goals.proteinGoal,
            carbsGoal = goals.carbsGoal,
            fatGoal = goals.fatGoal,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState()
    )

    fun selectDays(days: Int) {
        selectedDays.value = days
    }

    fun selectMetric(metric: StatisticsMetric) {
        selectedMetric.value = metric
    }
}