package com.moulgus.macrotracker.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity

data class MealWithEntries(
    @Embedded val meal: MealEntity,

    @Relation(
        parentColumn = "mealID",
        entityColumn = "mealID"
    )
    val entries: List<MealEntryEntity>
) {
    val kcal: Double
        get() = entries.sumOf { it.kcal }

    val protein: Double
        get() = entries.sumOf { it.protein }

    val carbs: Double
        get() = entries.sumOf { it.carbs }

    val fat: Double
        get() = entries.sumOf { it.fat }
}