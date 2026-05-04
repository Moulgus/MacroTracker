package com.moulgus.macrotracker.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MacroTrackerWidgetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            MacroTrackerWidgetUpdater.update(applicationContext)
            Result.success()
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}