package com.moulgus.macrotracker.widget

import android.content.Context
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.util.TrackingDateUtils
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

object MacroWidgetDataLoader {

    private val labelFormatter = DateTimeFormatter.ofPattern("dd.MM")

    suspend fun load(context: Context): MacroWidgetData {
        val app = context.applicationContext as MacroTrackerApplication

        val trackingDate = TrackingDateUtils.getCurrentTrackingDate()
        val trackingDateString = TrackingDateUtils.getCurrentTrackingDateString()

        val summary = app.repository
            .observeDailyMacroSummary(trackingDateString)
            .first()

        val goals = app.userSettingsRepository
            .goalsFlow
            .first()

        return MacroWidgetData(
            dateLabel = trackingDate.format(labelFormatter),

            eatenKcal = summary.kcal,
            kcalGoal = goals.kcalGoal,

            eatenProtein = summary.protein,
            proteinGoal = goals.proteinGoal,

            eatenCarbs = summary.carbs,
            carbsGoal = goals.carbsGoal,

            eatenFat = summary.fat,
            fatGoal = goals.fatGoal
        )
    }
}