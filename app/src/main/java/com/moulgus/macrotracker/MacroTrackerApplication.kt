package com.moulgus.macrotracker

import android.app.Application
import androidx.room.Room
import com.moulgus.macrotracker.data.local.database.AppDatabase
import com.moulgus.macrotracker.data.repository.MacroTrackerRepository
import com.moulgus.macrotracker.data.settings.UserSettingsRepository
import com.moulgus.macrotracker.data.settings.userSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MacroTrackerApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "macro_tracker_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository: MacroTrackerRepository by lazy {
        MacroTrackerRepository(
            productDao = database.productDao(),
            productUnitDao = database.productUnitDao(),
            mealDao = database.mealDao(),
            mealEntryDao = database.mealEntryDao()
        )
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(
            dataStore = applicationContext.userSettingsDataStore
        )
    }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            repository.seedDefaultProductsIfNeeded()
        }
    }
}