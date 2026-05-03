package com.moulgus.macrotracker.ui.screens.addmeal

import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity

data class AddMealUiState(
    val products: List<ProductEntity> = emptyList(),
    val selectedProduct: ProductEntity? = null,
    val productUnits: List<ProductUnitEntity> = emptyList(),

    val amountText: String = "",
    val selectedUnitName: String = "",

    val calculatedKcal: Double = 0.0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,

    val errorMessage: String? = null,
    val isLoading: Boolean = true
)