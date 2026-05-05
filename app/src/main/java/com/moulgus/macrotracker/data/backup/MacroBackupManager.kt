package com.moulgus.macrotracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.moulgus.macrotracker.data.local.database.AppDatabase
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntryEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntity
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import com.moulgus.macrotracker.data.settings.UserSettingsRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class MacroBackupManager(
    private val context: Context,
    private val database: AppDatabase,
    private val userSettingsRepository: UserSettingsRepository
) {

    suspend fun exportToUri(uri: Uri) {
        val backupDao = database.backupDao()
        val goals = userSettingsRepository.goalsFlow.first()

        val root = JSONObject()
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put(
                "goals",
                JSONObject()
                    .put("kcalGoal", goals.kcalGoal)
                    .put("proteinGoal", goals.proteinGoal)
                    .put("carbsGoal", goals.carbsGoal)
                    .put("fatGoal", goals.fatGoal)
            )
            .put(
                "products",
                toJsonArray(backupDao.getAllProducts()) { product ->
                    JSONObject()
                        .put("productID", product.productID)
                        .put("name", product.name)
                        .put("category", product.category)
                        .put("baseUnit", product.baseUnit)
                        .put("kcalPer100", product.kcalPer100)
                        .put("proteinPer100", product.proteinPer100)
                        .put("carbsPer100", product.carbsPer100)
                        .put("fatPer100", product.fatPer100)
                        .put("isCustom", product.isCustom)
                        .put("isFavorite", product.isFavorite)
                }
            )
            .put(
                "productUnits",
                toJsonArray(backupDao.getAllProductUnits()) { unit ->
                    JSONObject()
                        .put("unitID", unit.unitID)
                        .put("productID", unit.productID)
                        .put("unitName", unit.unitName)
                        .put("amountInBaseUnit", unit.amountInBaseUnit)
                }
            )
            .put(
                "meals",
                toJsonArray(backupDao.getAllMeals()) { meal ->
                    JSONObject()
                        .put("mealID", meal.mealID)
                        .put("name", meal.name ?: JSONObject.NULL)
                        .put("date", meal.date)
                        .put("createdAt", meal.createdAt)
                }
            )
            .put(
                "mealEntries",
                toJsonArray(backupDao.getAllMealEntries()) { entry ->
                    JSONObject()
                        .put("entryID", entry.entryID)
                        .put("mealID", entry.mealID)
                        .put("productID", entry.productID ?: JSONObject.NULL)
                        .put("productName", entry.productName)
                        .put("date", entry.date)
                        .put("amount", entry.amount)
                        .put("unitName", entry.unitName)
                        .put("amountInBaseUnit", entry.amountInBaseUnit)
                        .put("kcal", entry.kcal)
                        .put("protein", entry.protein)
                        .put("carbs", entry.carbs)
                        .put("fat", entry.fat)
                        .put("createdAt", entry.createdAt)
                }
            )
            .put(
                "mealTemplates",
                toJsonArray(backupDao.getAllMealTemplates()) { template ->
                    JSONObject()
                        .put("templateID", template.templateID)
                        .put("name", template.name)
                        .put("createdAt", template.createdAt)
                }
            )
            .put(
                "mealTemplateEntries",
                toJsonArray(backupDao.getAllMealTemplateEntries()) { entry ->
                    JSONObject()
                        .put("templateEntryID", entry.templateEntryID)
                        .put("templateID", entry.templateID)
                        .put("productID", entry.productID ?: JSONObject.NULL)
                        .put("productName", entry.productName)
                        .put("amount", entry.amount)
                        .put("unitName", entry.unitName)
                        .put("amountInBaseUnit", entry.amountInBaseUnit)
                        .put("kcal", entry.kcal)
                        .put("protein", entry.protein)
                        .put("carbs", entry.carbs)
                        .put("fat", entry.fat)
                }
            )

        val jsonText = root.toString(2)

        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(jsonText)
        } ?: throw IllegalStateException("Nie udało się otworzyć pliku do zapisu.")
    }

    suspend fun importFromUri(uri: Uri) {
        val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readText()
        } ?: throw IllegalStateException("Nie udało się otworzyć pliku do odczytu.")

        val root = JSONObject(jsonText)

        val products = parseProducts(root.optJSONArray("products") ?: JSONArray())
        val productUnits = parseProductUnits(root.optJSONArray("productUnits") ?: JSONArray())
        val meals = parseMeals(root.optJSONArray("meals") ?: JSONArray())
        val mealEntries = parseMealEntries(root.optJSONArray("mealEntries") ?: JSONArray())
        val mealTemplates = parseMealTemplates(root.optJSONArray("mealTemplates") ?: JSONArray())
        val mealTemplateEntries = parseMealTemplateEntries(
            root.optJSONArray("mealTemplateEntries") ?: JSONArray()
        )

        database.withTransaction {
            val backupDao = database.backupDao()

            backupDao.clearMealTemplateEntries()
            backupDao.clearMealTemplates()
            backupDao.clearMealEntries()
            backupDao.clearMeals()
            backupDao.clearProductUnits()
            backupDao.clearProducts()

            if (products.isNotEmpty()) {
                backupDao.insertProducts(products)
            }

            if (productUnits.isNotEmpty()) {
                backupDao.insertProductUnits(productUnits)
            }

            if (meals.isNotEmpty()) {
                backupDao.insertMeals(meals)
            }

            if (mealEntries.isNotEmpty()) {
                backupDao.insertMealEntries(mealEntries)
            }

            if (mealTemplates.isNotEmpty()) {
                backupDao.insertMealTemplates(mealTemplates)
            }

            if (mealTemplateEntries.isNotEmpty()) {
                backupDao.insertMealTemplateEntries(mealTemplateEntries)
            }
        }

        val goalsJson = root.optJSONObject("goals")

        if (goalsJson != null) {
            userSettingsRepository.updateGoals(
                kcalGoal = goalsJson.optDouble("kcalGoal", 2500.0),
                proteinGoal = goalsJson.optDouble("proteinGoal", 160.0),
                carbsGoal = goalsJson.optDouble("carbsGoal", 250.0),
                fatGoal = goalsJson.optDouble("fatGoal", 80.0)
            )
        }
    }

    private fun parseProducts(array: JSONArray): List<ProductEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    ProductEntity(
                        productID = item.getLong("productID"),
                        name = item.getString("name"),
                        category = item.getString("category"),
                        baseUnit = item.getString("baseUnit"),
                        kcalPer100 = item.getDouble("kcalPer100"),
                        proteinPer100 = item.getDouble("proteinPer100"),
                        carbsPer100 = item.getDouble("carbsPer100"),
                        fatPer100 = item.getDouble("fatPer100"),
                        isCustom = item.optBoolean("isCustom", true),
                        isFavorite = item.optBoolean("isFavorite", false)
                    )
                )
            }
        }
    }

    private fun parseProductUnits(array: JSONArray): List<ProductUnitEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    ProductUnitEntity(
                        unitID = item.getLong("unitID"),
                        productID = item.getLong("productID"),
                        unitName = item.getString("unitName"),
                        amountInBaseUnit = item.getDouble("amountInBaseUnit")
                    )
                )
            }
        }
    }

    private fun parseMeals(array: JSONArray): List<MealEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    MealEntity(
                        mealID = item.getLong("mealID"),
                        name = item.optStringOrNull("name"),
                        date = item.getString("date"),
                        createdAt = item.getLong("createdAt")
                    )
                )
            }
        }
    }

    private fun parseMealEntries(array: JSONArray): List<MealEntryEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    MealEntryEntity(
                        entryID = item.getLong("entryID"),
                        mealID = item.getLong("mealID"),
                        productID = item.optLongOrNull("productID"),
                        productName = item.getString("productName"),
                        date = item.getString("date"),
                        amount = item.getDouble("amount"),
                        unitName = item.getString("unitName"),
                        amountInBaseUnit = item.getDouble("amountInBaseUnit"),
                        kcal = item.getDouble("kcal"),
                        protein = item.getDouble("protein"),
                        carbs = item.getDouble("carbs"),
                        fat = item.getDouble("fat"),
                        createdAt = item.getLong("createdAt")
                    )
                )
            }
        }
    }

    private fun parseMealTemplates(array: JSONArray): List<MealTemplateEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    MealTemplateEntity(
                        templateID = item.getLong("templateID"),
                        name = item.getString("name"),
                        createdAt = item.getLong("createdAt")
                    )
                )
            }
        }
    }

    private fun parseMealTemplateEntries(array: JSONArray): List<MealTemplateEntryEntity> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    MealTemplateEntryEntity(
                        templateEntryID = item.getLong("templateEntryID"),
                        templateID = item.getLong("templateID"),
                        productID = item.optLongOrNull("productID"),
                        productName = item.getString("productName"),
                        amount = item.getDouble("amount"),
                        unitName = item.getString("unitName"),
                        amountInBaseUnit = item.getDouble("amountInBaseUnit"),
                        kcal = item.getDouble("kcal"),
                        protein = item.getDouble("protein"),
                        carbs = item.getDouble("carbs"),
                        fat = item.getDouble("fat")
                    )
                )
            }
        }
    }

    private fun <T> toJsonArray(
        items: List<T>,
        transform: (T) -> JSONObject
    ): JSONArray {
        val array = JSONArray()

        items.forEach { item ->
            array.put(transform(item))
        }

        return array
    }

    private fun JSONObject.optLongOrNull(name: String): Long? {
        return if (isNull(name)) {
            null
        } else {
            getLong(name)
        }
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        return if (isNull(name)) {
            null
        } else {
            getString(name)
        }
    }
}