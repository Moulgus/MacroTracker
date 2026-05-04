package com.moulgus.macrotracker.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object MacroTrackerWidgetUpdater {

    suspend fun update(context: Context) {
        val appContext = context.applicationContext

        val glanceIds = GlanceAppWidgetManager(appContext)
            .getGlanceIds(MacroTrackerWidget::class.java)

        glanceIds.forEach { glanceID ->
            MacroTrackerWidget().update(
                context = appContext,
                id = glanceID
            )
        }
    }
}