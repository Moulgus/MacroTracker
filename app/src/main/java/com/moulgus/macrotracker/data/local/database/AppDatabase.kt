package com.moulgus.macrotracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moulgus.macrotracker.data.local.dao.MealEntryDao
import com.moulgus.macrotracker.data.local.dao.ProductDao
import com.moulgus.macrotracker.data.local.dao.ProductUnitDao
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity

@Database(
    entities = [
        ProductEntity::class,
        ProductUnitEntity::class,
        MealEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun productUnitDao(): ProductUnitDao

    abstract fun mealEntryDao(): MealEntryDao
}