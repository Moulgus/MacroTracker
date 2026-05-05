package com.moulgus.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntity
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity

@Dao
interface BackupDao {

    @Query("SELECT * FROM products ORDER BY productID ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM product_units ORDER BY unitID ASC")
    suspend fun getAllProductUnits(): List<ProductUnitEntity>

    @Query("SELECT * FROM meals ORDER BY mealID ASC")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM meal_entries ORDER BY entryID ASC")
    suspend fun getAllMealEntries(): List<MealEntryEntity>

    @Query("SELECT * FROM meal_templates ORDER BY templateID ASC")
    suspend fun getAllMealTemplates(): List<MealTemplateEntity>

    @Query("SELECT * FROM meal_template_entries ORDER BY templateEntryID ASC")
    suspend fun getAllMealTemplateEntries(): List<MealTemplateEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductUnits(units: List<ProductUnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealEntries(entries: List<MealEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealTemplates(templates: List<MealTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealTemplateEntries(entries: List<MealTemplateEntryEntity>)

    @Query("DELETE FROM meal_template_entries")
    suspend fun clearMealTemplateEntries()

    @Query("DELETE FROM meal_templates")
    suspend fun clearMealTemplates()

    @Query("DELETE FROM meal_entries")
    suspend fun clearMealEntries()

    @Query("DELETE FROM meals")
    suspend fun clearMeals()

    @Query("DELETE FROM product_units")
    suspend fun clearProductUnits()

    @Query("DELETE FROM products")
    suspend fun clearProducts()
}