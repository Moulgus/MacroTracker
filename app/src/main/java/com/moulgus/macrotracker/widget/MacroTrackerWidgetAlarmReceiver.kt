package com.moulgus.macrotracker.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MacroTrackerWidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            MacroTrackerWidgetUpdater.update(context.applicationContext)
            MacroTrackerWidgetAlarmScheduler.scheduleNextRefresh(context.applicationContext)
        }
    }
}