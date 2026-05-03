package com.moulgus.macrotracker.ui.screens.statistics

data class StatisticsDayItem(
    val date: String,
    val label: String,

    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
) {
    fun valueForMetric(metric: StatisticsMetric): Double {
        return when (metric) {
            StatisticsMetric.KCAL -> kcal
            StatisticsMetric.PROTEIN -> protein
            StatisticsMetric.CARBS -> carbs
            StatisticsMetric.FAT -> fat
        }
    }
}