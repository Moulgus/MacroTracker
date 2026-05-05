package com.moulgus.macrotracker.ui.screens.products

import com.moulgus.macrotracker.data.local.entity.ProductEntity

data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),

    val searchQuery: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Wszystkie",

    val editProductID: Long? = null,
    val isEditMode: Boolean = false,

    val form: ProductFormState = ProductFormState(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = true
)