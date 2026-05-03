package com.moulgus.macrotracker.ui.screens.products

data class ProductFormState(
    val nameText: String = "",
    val categoryText: String = "Inne",
    val baseUnit: String = "g",

    val kcalText: String = "",
    val proteinText: String = "",
    val carbsText: String = "",
    val fatText: String = ""
)