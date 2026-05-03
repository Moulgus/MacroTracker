package com.moulgus.macrotracker.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userSettingsDataStore by preferencesDataStore(
    name = "user_settings"
)

class UserSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val KCAL_GOAL = doublePreferencesKey("kcal_goal")
        val PROTEIN_GOAL = doublePreferencesKey("protein_goal")
        val CARBS_GOAL = doublePreferencesKey("carbs_goal")
        val FAT_GOAL = doublePreferencesKey("fat_goal")
    }

    val goalsFlow: Flow<UserMacroGoals> = dataStore.data.map { preferences ->
        UserMacroGoals(
            kcalGoal = preferences[Keys.KCAL_GOAL] ?: 2500.0,
            proteinGoal = preferences[Keys.PROTEIN_GOAL] ?: 160.0,
            carbsGoal = preferences[Keys.CARBS_GOAL] ?: 250.0,
            fatGoal = preferences[Keys.FAT_GOAL] ?: 80.0
        )
    }

    suspend fun updateGoals(
        kcalGoal: Double,
        proteinGoal: Double,
        carbsGoal: Double,
        fatGoal: Double
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.KCAL_GOAL] = kcalGoal
            preferences[Keys.PROTEIN_GOAL] = proteinGoal
            preferences[Keys.CARBS_GOAL] = carbsGoal
            preferences[Keys.FAT_GOAL] = fatGoal
        }
    }
}