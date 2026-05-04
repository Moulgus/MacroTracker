package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_template_entries",
    foreignKeys = [
        ForeignKey(
            entity = MealTemplateEntity::class,
            parentColumns = ["templateID"],
            childColumns = ["templateID"],
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
        Index(value = ["templateID"]),
        Index(value = ["productID"])
    ]
)
data class MealTemplateEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val templateEntryID: Long = 0,

    val templateID: Long,

    val productID: Long?,

    val productName: String,

    val amount: Double,
    val unitName: String,
    val amountInBaseUnit: Double,

    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)