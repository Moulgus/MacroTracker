package com.moulgus.macrotracker.data.repository

data class MealIngredientDraft(
    val productID: Long?,
    val productName: String,
    val amount: Double,
    val unitName: String,
    val amountInBaseUnit: Double,

    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)