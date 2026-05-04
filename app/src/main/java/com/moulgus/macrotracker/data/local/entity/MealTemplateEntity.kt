package com.moulgus.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_templates")
data class MealTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val templateID: Long = 0,

    val name: String,

    val createdAt: Long = System.currentTimeMillis()
)