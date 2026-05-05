package com.moulgus.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntity
import com.moulgus.macrotracker.data.local.entity.MealTemplateEntryEntity
import com.moulgus.macrotracker.data.local.model.MealTemplateWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTemplateDao {

    @Transaction
    @Query("""
        SELECT * FROM meal_templates
        ORDER BY name ASC
    """)
    fun observeTemplatesWithEntries(): Flow<List<MealTemplateWithEntries>>

    @Transaction
    @Query("""
        SELECT * FROM meal_templates
        WHERE templateID = :templateID
        LIMIT 1
    """)
    suspend fun getTemplateWithEntriesByID(templateID: Long): MealTemplateWithEntries?

    @Query("""
    SELECT COUNT(*) FROM meal_templates
    WHERE LOWER(name) = LOWER(:name)
    """)
    suspend fun getTemplateCountByName(name: String): Int

    @Insert
    suspend fun insertTemplate(template: MealTemplateEntity): Long

    @Insert
    suspend fun insertTemplateEntries(entries: List<MealTemplateEntryEntity>)

    @Transaction
    suspend fun insertTemplateWithEntries(
        template: MealTemplateEntity,
        entries: List<MealTemplateEntryEntity>
    ): Long {
        val templateID = insertTemplate(template)

        val entriesWithTemplateID = entries.map { entry ->
            entry.copy(templateID = templateID)
        }

        insertTemplateEntries(entriesWithTemplateID)

        return templateID
    }

    @Query("DELETE FROM meal_templates WHERE templateID = :templateID")
    suspend fun deleteTemplateByID(templateID: Long)


}