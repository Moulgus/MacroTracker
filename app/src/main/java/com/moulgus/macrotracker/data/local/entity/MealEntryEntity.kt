package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["mealID"],
            childColumns = ["mealID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productID"],
            childColumns = ["productID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["mealID"]),
        Index(value = ["productID"]),
        Index(value = ["date"])
    ]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryID: Long = 0,

    val mealID: Long,

    val productID: Long?,

    val productName: String,

    // Trzymamy też datę przy składniku, żeby statystyki dalej były proste i szybkie.
    val date: String,

    val amount: Double,
    val unitName: String,
    val amountInBaseUnit: Double,

    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    val createdAt: Long = System.currentTimeMillis()
)