package com.moulgus.macrotracker.ui.screens.statistics

data class StatisticsUiState(
    val selectedDays: Int = 7,
    val selectedMetric: StatisticsMetric = StatisticsMetric.KCAL,

    val days: List<StatisticsDayItem> = emptyList(),

    val kcalGoal: Double = 2500.0,
    val proteinGoal: Double = 160.0,
    val carbsGoal: Double = 250.0,
    val fatGoal: Double = 80.0,

    val isLoading: Boolean = true
) {
    val selectedGoal: Double
        get() = when (selectedMetric) {
            StatisticsMetric.KCAL -> kcalGoal
            StatisticsMetric.PROTEIN -> proteinGoal
            StatisticsMetric.CARBS -> carbsGoal
            StatisticsMetric.FAT -> fatGoal
        }

    val averageValue: Double
        get() {
            if (days.isEmpty()) {
                return 0.0
            }

            return days.sumOf { it.valueForMetric(selectedMetric) } / days.size
        }

    val highestValue: Double
        get() {
            return days.maxOfOrNull { it.valueForMetric(selectedMetric) } ?: 0.0
        }

    val lowestValue: Double
        get() {
            return days.minOfOrNull { it.valueForMetric(selectedMetric) } ?: 0.0
        }
}