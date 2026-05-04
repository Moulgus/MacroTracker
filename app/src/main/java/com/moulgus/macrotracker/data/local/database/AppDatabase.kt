package com.moulgus.macrotracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moulgus.macrotracker.data.local.dao.MealDao
import com.moulgus.macrotracker.data.local.dao.MealEntryDao
import com.moulgus.macrotracker.data.local.dao.MealTemplateDao
import com.moulgus.macrotracker.data.local.dao.ProductDao
import com.moulgus.macrotracker.data.local.dao.ProductUnitDao
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntity
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity

@Database(
    entities = [
        ProductEntity::class,
        ProductUnitEntity::class,
        MealEntity::class,
        MealEntryEntity::class,
        MealTemplateEntity::class,
        MealTemplateEntryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun productUnitDao(): ProductUnitDao

    abstract fun mealDao(): MealDao

    abstract fun mealEntryDao(): MealEntryDao

    abstract fun mealTemplateDao(): MealTemplateDao
}