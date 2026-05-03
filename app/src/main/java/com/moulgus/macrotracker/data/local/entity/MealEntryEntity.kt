package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productID"],
            childColumns = ["productID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["productID"]),
        Index(value = ["date"])
    ]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryID: Long = 0,

    // Może być null, jeśli produkt kiedyś zostanie usunięty.
    // Historia jedzenia zostanie zachowana.
    val productID: Long?,

    // Snapshot nazwy produktu w momencie dodania.
    // Dzięki temu historia nie psuje się po edycji/usunięciu produktu.
    val productName: String,

    // Data w formacie "yyyy-MM-dd", np. "2026-05-03".
    val date: String,

    // Ilość wpisana przez użytkownika, np. 2.0.
    val amount: Double,

    // Jednostka wybrana przez użytkownika, np. "g", "ml", "łyżeczka".
    val unitName: String,

    // Ilość po przeliczeniu na jednostkę bazową produktu.
    // Np. 2 łyżeczki miodu po 12 g = 24.0.
    val amountInBaseUnit: Double,

    // Snapshot wyliczonych wartości w momencie dodania.
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    // Czas utworzenia wpisu.
    val createdAt: Long = System.currentTimeMillis()
)