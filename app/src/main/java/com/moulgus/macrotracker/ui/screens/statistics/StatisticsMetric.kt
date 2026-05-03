package com.moulgus.macrotracker.ui.screens.statistics

enum class StatisticsMetric(
    val label: String,
    val unit: String
) {
    KCAL(label = "Kcal", unit = "kcal"),
    PROTEIN(label = "Białko", unit = "g"),
    CARBS(label = "Węgle", unit = "g"),
    FAT(label = "Tłuszcz", unit = "g")
}