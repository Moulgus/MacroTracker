package com.moulgus.macrotracker.ui.screens.addmeal

import com.moulgus.macrotracker.data.repository.MealIngredientDraft

data class DraftMealIngredient(
    val draftIngredientID: Long,
    val productID: Long?,
    val productName: String,
    val amount: Double,
    val unitName: String,
    val amountInBaseUnit: Double,

    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
) {
    fun toRepositoryDraft(): MealIngredientDraft {
        return MealIngredientDraft(
            productID = productID,
            productName = productName,
            amount = amount,
            unitName = unitName,
            amountInBaseUnit = amountInBaseUnit,
            kcal = kcal,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }
}