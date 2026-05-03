package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_units",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productID"],
            childColumns = ["productID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productID"])
    ]
)
data class ProductUnitEntity(
    @PrimaryKey(autoGenerate = true)
    val unitID: Long = 0,

    val productID: Long,

    // Np. "łyżeczka", "łyżka", "szklanka", "sztuka".
    val unitName: String,

    // Ile ta jednostka ma w jednostce bazowej produktu.
    //
    // Przykłady:
    // Miód ma baseUnit = "g"
    // 1 łyżeczka miodu = 12 g
    // amountInBaseUnit = 12.0
    //
    // Mleko ma baseUnit = "ml"
    // 1 szklanka mleka = 250 ml
    // amountInBaseUnit = 250.0
    val amountInBaseUnit: Double
)