package com.moulgus.macrotracker.data.repository

import com.moulgus.macrotracker.data.local.dao.MealDao
import com.moulgus.macrotracker.data.local.dao.MealEntryDao
import com.moulgus.macrotracker.data.local.dao.ProductDao
import com.moulgus.macrotracker.data.local.dao.ProductUnitDao
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import com.moulgus.macrotracker.data.local.model.DailyMacroSummary
import com.moulgus.macrotracker.data.local.model.MealWithEntries
import kotlinx.coroutines.flow.Flow
import com.moulgus.macrotracker.data.local.dao.MealTemplateDao
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntity
import com.moulgus.macrotracker.data.local.model.MealTemplateWithEntries

class MacroTrackerRepository(
    private val productDao: ProductDao,
    private val productUnitDao: ProductUnitDao,
    private val mealDao: MealDao,
    private val mealTemplateDao: MealTemplateDao,
    private val mealEntryDao: MealEntryDao
) {

    fun observeAllProducts(): Flow<List<ProductEntity>> {
        return productDao.observeAllProducts()
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    suspend fun getProductByID(productID: Long): ProductEntity? {
        return productDao.getProductByID(productID)
    }

    suspend fun addProduct(
        name: String,
        category: String,
        baseUnit: String,
        kcalPer100: Double,
        proteinPer100: Double,
        carbsPer100: Double,
        fatPer100: Double,
        isCustom: Boolean = true
    ): Long {
        val product = ProductEntity(
            name = name,
            category = category,
            baseUnit = baseUnit,
            kcalPer100 = kcalPer100,
            proteinPer100 = proteinPer100,
            carbsPer100 = carbsPer100,
            fatPer100 = fatPer100,
            isCustom = isCustom
        )

        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun updateProductFavorite(
        productID: Long,
        isFavorite: Boolean
    ) {
        productDao.updateProductFavorite(
            productID = productID,
            isFavorite = isFavorite
        )
    }

    fun observeUnitsForProduct(productID: Long): Flow<List<ProductUnitEntity>> {
        return productUnitDao.observeUnitsForProduct(productID)
    }

    suspend fun getUnitsForProduct(productID: Long): List<ProductUnitEntity> {
        return productUnitDao.getUnitsForProduct(productID)
    }

    suspend fun addProductUnit(
        productID: Long,
        unitName: String,
        amountInBaseUnit: Double
    ): Long {
        val unit = ProductUnitEntity(
            productID = productID,
            unitName = unitName,
            amountInBaseUnit = amountInBaseUnit
        )

        return productUnitDao.insertProductUnit(unit)
    }

    suspend fun updateProductUnit(unit: ProductUnitEntity) {
        productUnitDao.updateProductUnit(unit)
    }

    suspend fun deleteProductUnit(unit: ProductUnitEntity) {
        productUnitDao.deleteProductUnit(unit)
    }

    fun observeMealsWithEntriesForDate(date: String): Flow<List<MealWithEntries>> {
        return mealDao.observeMealsWithEntriesForDate(date)
    }

    suspend fun getMealWithEntriesByID(mealID: Long): MealWithEntries? {
        return mealDao.getMealWithEntriesByID(mealID)
    }

    suspend fun addMeal(
        date: String,
        name: String?,
        ingredients: List<MealIngredientDraft>
    ): Long {
        val cleanName = name
            ?.trim()
            ?.ifBlank { null }

        val meal = MealEntity(
            name = cleanName,
            date = date
        )

        val entries = ingredients.map { ingredient ->
            MealEntryEntity(
                mealID = 0,
                productID = ingredient.productID,
                productName = ingredient.productName,
                date = date,
                amount = ingredient.amount,
                unitName = ingredient.unitName,
                amountInBaseUnit = ingredient.amountInBaseUnit,
                kcal = ingredient.kcal,
                protein = ingredient.protein,
                carbs = ingredient.carbs,
                fat = ingredient.fat
            )
        }

        return mealDao.insertMealWithEntries(
            meal = meal,
            entries = entries
        )
    }

    suspend fun updateMeal(
        mealID: Long,
        date: String,
        name: String?,
        ingredients: List<MealIngredientDraft>
    ) {
        val cleanName = name
            ?.trim()
            ?.ifBlank { null }

        val entries = ingredients.map { ingredient ->
            MealEntryEntity(
                mealID = mealID,
                productID = ingredient.productID,
                productName = ingredient.productName,
                date = date,
                amount = ingredient.amount,
                unitName = ingredient.unitName,
                amountInBaseUnit = ingredient.amountInBaseUnit,
                kcal = ingredient.kcal,
                protein = ingredient.protein,
                carbs = ingredient.carbs,
                fat = ingredient.fat
            )
        }

        mealDao.updateMealWithEntries(
            mealID = mealID,
            name = cleanName,
            date = date,
            entries = entries
        )
    }

    fun observeMealTemplatesWithEntries(): Flow<List<MealTemplateWithEntries>> {
        return mealTemplateDao.observeTemplatesWithEntries()
    }

    suspend fun getMealTemplateWithEntriesByID(templateID: Long): MealTemplateWithEntries? {
        return mealTemplateDao.getTemplateWithEntriesByID(templateID)
    }

    suspend fun addMealTemplate(
        name: String,
        ingredients: List<MealIngredientDraft>
    ): Long {
        val template = MealTemplateEntity(
            name = name.trim()
        )

        val entries = ingredients.map { ingredient ->
            MealTemplateEntryEntity(
                templateID = 0,
                productID = ingredient.productID,
                productName = ingredient.productName,
                amount = ingredient.amount,
                unitName = ingredient.unitName,
                amountInBaseUnit = ingredient.amountInBaseUnit,
                kcal = ingredient.kcal,
                protein = ingredient.protein,
                carbs = ingredient.carbs,
                fat = ingredient.fat
            )
        }

        return mealTemplateDao.insertTemplateWithEntries(
            template = template,
            entries = entries
        )
    }

    suspend fun deleteMealTemplateByID(templateID: Long) {
        mealTemplateDao.deleteTemplateByID(templateID)
    }
    suspend fun deleteMealByID(mealID: Long) {
        mealDao.deleteMealByID(mealID)
    }

    fun observeEntriesForDate(date: String): Flow<List<MealEntryEntity>> {
        return mealEntryDao.observeEntriesForDate(date)
    }

    fun observeEntriesBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<MealEntryEntity>> {
        return mealEntryDao.observeEntriesBetweenDates(startDate, endDate)
    }

    fun observeDailyMacroSummary(date: String): Flow<DailyMacroSummary> {
        return mealEntryDao.observeDailyMacroSummary(date)
    }

    suspend fun seedDefaultProductsIfNeeded() {
        val productCount = productDao.getProductCount()

        if (productCount > 0) {
            return
        }

        val riceID = addProduct(
            name = "Ryż biały",
            category = "Produkty sypkie",
            baseUnit = "g",
            kcalPer100 = 360.0,
            proteinPer100 = 7.0,
            carbsPer100 = 79.0,
            fatPer100 = 0.7,
            isCustom = false
        )

        addProduct(
            name = "Pierś z kurczaka surowa",
            category = "Mięso",
            baseUnit = "g",
            kcalPer100 = 110.0,
            proteinPer100 = 23.0,
            carbsPer100 = 0.0,
            fatPer100 = 1.5,
            isCustom = false
        )

        addProduct(
            name = "Brokuły",
            category = "Warzywa",
            baseUnit = "g",
            kcalPer100 = 35.0,
            proteinPer100 = 2.4,
            carbsPer100 = 7.2,
            fatPer100 = 0.4,
            isCustom = false
        )

        addProduct(
            name = "Skyr naturalny",
            category = "Nabiał",
            baseUnit = "g",
            kcalPer100 = 65.0,
            proteinPer100 = 12.0,
            carbsPer100 = 4.0,
            fatPer100 = 0.2,
            isCustom = false
        )

        addProduct(
            name = "Twaróg chudy",
            category = "Nabiał",
            baseUnit = "g",
            kcalPer100 = 95.0,
            proteinPer100 = 19.0,
            carbsPer100 = 3.5,
            fatPer100 = 0.5,
            isCustom = false
        )

        val bananaID = addProduct(
            name = "Banan",
            category = "Owoce",
            baseUnit = "g",
            kcalPer100 = 89.0,
            proteinPer100 = 1.1,
            carbsPer100 = 23.0,
            fatPer100 = 0.3,
            isCustom = false
        )

        val honeyID = addProduct(
            name = "Miód",
            category = "Dodatki",
            baseUnit = "g",
            kcalPer100 = 320.0,
            proteinPer100 = 0.3,
            carbsPer100 = 80.0,
            fatPer100 = 0.0,
            isCustom = false
        )

        val kefirID = addProduct(
            name = "Kefir naturalny",
            category = "Nabiał",
            baseUnit = "ml",
            kcalPer100 = 50.0,
            proteinPer100 = 3.4,
            carbsPer100 = 4.7,
            fatPer100 = 2.0,
            isCustom = false
        )

        val eggID = addProduct(
            name = "Jajko",
            category = "Nabiał",
            baseUnit = "g",
            kcalPer100 = 143.0,
            proteinPer100 = 13.0,
            carbsPer100 = 1.0,
            fatPer100 = 10.0,
            isCustom = false
        )

        addProductUnit(
            productID = riceID,
            unitName = "woreczek 100 g",
            amountInBaseUnit = 100.0
        )

        addProductUnit(
            productID = honeyID,
            unitName = "łyżeczka",
            amountInBaseUnit = 12.0
        )

        addProductUnit(
            productID = honeyID,
            unitName = "łyżka",
            amountInBaseUnit = 25.0
        )

        addProductUnit(
            productID = kefirID,
            unitName = "szklanka",
            amountInBaseUnit = 250.0
        )

        addProductUnit(
            productID = eggID,
            unitName = "sztuka",
            amountInBaseUnit = 55.0
        )

        addProductUnit(
            productID = bananaID,
            unitName = "średnia sztuka",
            amountInBaseUnit = 120.0
        )
    }
}