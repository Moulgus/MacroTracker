package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val productID: Long = 0,

    val name: String,

    // Na razie zapisujemy jako tekst, np. "Mięso", "Nabiał", "Produkty sypkie", "Inne".
    // Później możemy zrobić z tego realne filtrowanie.
    val category: String = "Inne",

    // "g" albo "ml".
    // Dla większości jedzenia będzie "g", dla płynów "ml".
    val baseUnit: String = "g",

    // Wartości zawsze dla 100 g albo 100 ml, zgodnie z etykietą produktu.
    val kcalPer100: Double,
    val proteinPer100: Double,
    val carbsPer100: Double,
    val fatPer100: Double,

    // true = produkt dodany przez użytkownika
    // false = produkt startowy/domyślny z aplikacji
    val isCustom: Boolean = true
)