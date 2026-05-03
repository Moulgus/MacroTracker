package com.moulgus.macrotracker.ui.screens.products

import com.moulgus.macrotracker.data.local.entity.ProductEntity

data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val form: ProductFormState = ProductFormState(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = true
)