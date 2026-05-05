package com.moulgus.macrotracker.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.moulgus.macrotracker.MainActivity
import androidx.compose.runtime.Composable
import androidx.glance.layout.Box
import com.moulgus.macrotracker.util.formatSmart

class MacroTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val data = MacroWidgetDataLoader.load(context)

        provideContent {
            MacroTrackerWidgetContent(data = data)
        }
    }
}

@Composable
private fun MacroTrackerWidgetContent(
    data: MacroWidgetData
) {
    val backgroundColor = ColorProvider(Color(0xFF49CCB5))
    val primaryTextColor = ColorProvider(Color(0xFF00201B))
    val secondaryTextColor = ColorProvider(Color(0xFF003D35))
    val dateTextColor = ColorProvider(Color(0xFF005E53))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp)
    ) {
        // Data — prawy górny róg
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = data.dateLabel,
                style = TextStyle(
                    color = dateTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }

        // Kcal — wyżej niż środek, ale niżej niż data
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(top = 42.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${data.eatenKcal.formatSmart(0)} / ${data.kcalGoal.formatSmart(0)} kcal",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // B/W/T — lewy dół
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "B: ${data.eatenProtein.formatSmart(0)} / ${data.proteinGoal.formatSmart(0)} g",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = "W: ${data.eatenCarbs.formatSmart(0)} / ${data.carbsGoal.formatSmart(0)} g",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = "T: ${data.eatenFat.formatSmart(0)} / ${data.fatGoal.formatSmart(0)} g",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}