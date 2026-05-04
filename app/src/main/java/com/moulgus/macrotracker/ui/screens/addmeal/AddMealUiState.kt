package com.moulgus.macrotracker.ui.screens.addmeal

import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import com.moulgus.macrotracker.data.local.model.MealTemplateWithEntries

data class AddMealUiState(
    val products: List<ProductEntity> = emptyList(),
    val selectedProduct: ProductEntity? = null,
    val productUnits: List<ProductUnitEntity> = emptyList(),

    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Wszystkie",

    val templates: List<MealTemplateWithEntries> = emptyList(),
    val showTemplates: Boolean = false,
    val templateSavedMessage: String? = null,

    val editMealID: Long? = null,
    val isEditMode: Boolean = false,

    val searchQuery: String = "",
    val mealNameText: String = "",

    val selectedDate: String = "",
    val selectedDateLabel: String = "",
    val canMoveToNextDay: Boolean = false,

    val amountText: String = "",
    val selectedUnitName: String = "",

    val calculatedKcal: Double = 0.0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,

    val ingredients: List<DraftMealIngredient> = emptyList(),

    val totalKcal: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,

    val errorMessage: String? = null,
    val isLoading: Boolean = true


)