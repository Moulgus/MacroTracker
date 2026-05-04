package com.moulgus.macrotracker.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object MacroTrackerWidgetAlarmScheduler {

    private const val REQUEST_CODE = 4001

    fun scheduleNextRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = createPendingIntent(context)

        val triggerAtMillis = calculateNextRefreshMillis()

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MacroTrackerWidgetAlarmReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateNextRefreshMillis(): Long {
        val now = LocalDateTime.now()

        var nextRefresh = now.toLocalDate()
            .atTime(LocalTime.of(4, 0))
            .plusSeconds(5)

        if (!now.isBefore(nextRefresh)) {
            nextRefresh = nextRefresh.plusDays(1)
        }

        return nextRefresh
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}