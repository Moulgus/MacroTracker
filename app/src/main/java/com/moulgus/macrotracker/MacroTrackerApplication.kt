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
import com.moulgus.macrotracker.widget.MacroTrackerWidgetUpdater
import com.moulgus.macrotracker.data.local.database.MIGRATION_2_3
import com.moulgus.macrotracker.widget.MacroTrackerWidgetAlarmScheduler
import com.moulgus.macrotracker.data.local.database.MIGRATION_3_4
import com.moulgus.macrotracker.data.backup.MacroBackupManager

class MacroTrackerApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "macro_tracker_database"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val repository: MacroTrackerRepository by lazy {
        MacroTrackerRepository(
            productDao = database.productDao(),
            productUnitDao = database.productUnitDao(),
            mealDao = database.mealDao(),
            mealTemplateDao = database.mealTemplateDao(),
            mealEntryDao = database.mealEntryDao()
        )
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(
            dataStore = applicationContext.userSettingsDataStore
        )
    }

    val backupManager: MacroBackupManager by lazy {
        MacroBackupManager(
            context = applicationContext,
            database = database,
            userSettingsRepository = userSettingsRepository
        )
    }

    fun refreshWidgets() {
        applicationScope.launch {
            MacroTrackerWidgetUpdater.update(applicationContext)
        }
    }

    override fun onCreate() {
        super.onCreate()

        MacroTrackerWidgetAlarmScheduler.scheduleNextRefresh(applicationContext)

        applicationScope.launch {
            repository.seedDefaultProductsIfNeeded()
            MacroTrackerWidgetUpdater.update(applicationContext)
        }
    }
}