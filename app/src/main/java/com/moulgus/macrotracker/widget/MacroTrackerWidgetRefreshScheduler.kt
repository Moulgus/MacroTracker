package com.moulgus.macrotracker.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object MacroTrackerWidgetRefreshScheduler {

    private const val UNIQUE_WORK_NAME = "macro_tracker_widget_daily_refresh"

    fun scheduleDailyRefresh(context: Context) {
        val delay = calculateDelayUntilNextRefresh()

        val request = PeriodicWorkRequestBuilder<MacroTrackerWidgetRefreshWorker>(
            24,
            TimeUnit.HOURS
        )
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager
            .getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
            )
    }

    private fun calculateDelayUntilNextRefresh(): Duration {
        val now = LocalDateTime.now()

        var nextRefresh = now.toLocalDate()
            .atTime(LocalTime.of(4, 0))

        if (!now.isBefore(nextRefresh)) {
            nextRefresh = nextRefresh.plusDays(1)
        }

        return Duration.between(now, nextRefresh)
    }
}