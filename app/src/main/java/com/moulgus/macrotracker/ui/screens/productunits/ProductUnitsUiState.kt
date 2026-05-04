package com.moulgus.macrotracker.ui.screens.productunits

import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity

data class ProductUnitsUiState(
    val product: ProductEntity? = null,
    val units: List<ProductUnitEntity> = emptyList(),

    val unitNameText: String = "",
    val amountInBaseUnitText: String = "",

    val errorMessage: String? = null,
    val successMessage: String? = null,

    val isLoading: Boolean = true
)