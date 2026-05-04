package com.moulgus.macrotracker.widget

data class MacroWidgetData(
    val dateLabel: String,

    val eatenKcal: Double,
    val kcalGoal: Double,

    val eatenProtein: Double,
    val proteinGoal: Double,

    val eatenCarbs: Double,
    val carbsGoal: Double,

    val eatenFat: Double,
    val fatGoal: Double
)