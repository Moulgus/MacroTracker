package com.moulgus.macrotracker.ui.screens.today

import com.moulgus.macrotracker.data.local.model.MealWithEntries

data class TodayUiState(
    val date: String = "",
    val meals: List<MealWithEntries> = emptyList(),

    val eatenKcal: Double = 0.0,
    val eatenProtein: Double = 0.0,
    val eatenCarbs: Double = 0.0,
    val eatenFat: Double = 0.0,

    val kcalGoal: Double = 2500.0,
    val proteinGoal: Double = 160.0,
    val carbsGoal: Double = 250.0,
    val fatGoal: Double = 80.0,

    val isLoading: Boolean = true
) {
    val remainingKcal: Double
        get() = (kcalGoal - eatenKcal).coerceAtLeast(0.0)

    val remainingProtein: Double
        get() = (proteinGoal - eatenProtein).coerceAtLeast(0.0)

    val remainingCarbs: Double
        get() = (carbsGoal - eatenCarbs).coerceAtLeast(0.0)

    val remainingFat: Double
        get() = (fatGoal - eatenFat).coerceAtLeast(0.0)
}