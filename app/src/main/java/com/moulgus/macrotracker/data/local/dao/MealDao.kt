package com.moulgus.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.moulgus.macrotracker.data.local.entity.MealEntity
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.model.MealWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Transaction
    @Query("""
        SELECT * FROM meals
        WHERE date = :date
        ORDER BY createdAt DESC
    """)
    fun observeMealsWithEntriesForDate(date: String): Flow<List<MealWithEntries>>

    @Transaction
    @Query("""
        SELECT * FROM meals
        WHERE mealID = :mealID
        LIMIT 1
    """)
    suspend fun getMealWithEntriesByID(mealID: Long): MealWithEntries?

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert
    suspend fun insertMealEntries(entries: List<MealEntryEntity>)

    @Query("""
        UPDATE meals
        SET name = :name,
            date = :date
        WHERE mealID = :mealID
    """)
    suspend fun updateMealHeader(
        mealID: Long,
        name: String?,
        date: String
    )

    @Query("DELETE FROM meal_entries WHERE mealID = :mealID")
    suspend fun deleteEntriesForMeal(mealID: Long)

    @Transaction
    suspend fun insertMealWithEntries(
        meal: MealEntity,
        entries: List<MealEntryEntity>
    ): Long {
        val mealID = insertMeal(meal)

        val entriesWithMealID = entries.map { entry ->
            entry.copy(mealID = mealID)
        }

        insertMealEntries(entriesWithMealID)

        return mealID
    }

    @Transaction
    suspend fun updateMealWithEntries(
        mealID: Long,
        name: String?,
        date: String,
        entries: List<MealEntryEntity>
    ) {
        updateMealHeader(
            mealID = mealID,
            name = name,
            date = date
        )

        deleteEntriesForMeal(mealID)

        val entriesWithMealID = entries.map { entry ->
            entry.copy(
                entryID = 0,
                mealID = mealID,
                date = date
            )
        }

        insertMealEntries(entriesWithMealID)
    }

    @Query("DELETE FROM meals WHERE mealID = :mealID")
    suspend fun deleteMealByID(mealID: Long)
}