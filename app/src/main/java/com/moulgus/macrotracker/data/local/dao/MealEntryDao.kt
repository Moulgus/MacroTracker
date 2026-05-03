package com.moulgus.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import com.moulgus.macrotracker.data.local.model.DailyMacroSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Query("""
        SELECT * FROM meal_entries 
        WHERE date = :date 
        ORDER BY createdAt DESC
    """)
    fun observeEntriesForDate(date: String): Flow<List<MealEntryEntity>>

    @Query("""
        SELECT * FROM meal_entries 
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeEntriesBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<MealEntryEntity>>

    @Query("""
        SELECT 
            COALESCE(SUM(kcal), 0.0) AS kcal,
            COALESCE(SUM(protein), 0.0) AS protein,
            COALESCE(SUM(carbs), 0.0) AS carbs,
            COALESCE(SUM(fat), 0.0) AS fat
        FROM meal_entries
        WHERE date = :date
    """)
    fun observeDailyMacroSummary(date: String): Flow<DailyMacroSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealEntry(entry: MealEntryEntity): Long

    @Delete
    suspend fun deleteMealEntry(entry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE entryID = :entryID")
    suspend fun deleteMealEntryByID(entryID: Long)
}