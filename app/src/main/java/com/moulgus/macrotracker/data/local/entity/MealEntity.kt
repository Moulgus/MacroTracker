package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val mealID: Long = 0,

    // Opcjonalna nazwa, np. "Obiad", "Kolacja". Może być null.
    val name: String? = null,

    // Data trackingowa, czyli po naszej zasadzie dnia od 4:00.
    val date: String,

    val createdAt: Long = System.currentTimeMillis()
)