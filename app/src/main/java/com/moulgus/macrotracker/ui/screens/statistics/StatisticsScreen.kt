package com.moulgus.macrotracker.ui.screens.statistics

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max
import com.moulgus.macrotracker.util.formatSmart

@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onDaysClick = viewModel::selectDays,
        onMetricClick = viewModel::selectMetric
    )
}

@Composable
private fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    onBackClick: () -> Unit,
    onDaysClick: (Int) -> Unit,
    onMetricClick: (StatisticsMetric) -> Unit
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Statystyki",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(onClick = onBackClick) {
                        Text(text = "Wróć")
                    }
                }
            }

            item {
                RangeSelector(
                    selectedDays = uiState.selectedDays,
                    onDaysClick = onDaysClick
                )
            }

            item {
                MetricSelector(
                    selectedMetric = uiState.selectedMetric,
                    onMetricClick = onMetricClick
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Wykres: ${uiState.selectedMetric.label}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        LineMacroChart(
                            days = uiState.days,
                            selectedMetric = uiState.selectedMetric,
                            goal = uiState.selectedGoal
                        )
                    }
                }
            }

            item {
                StatisticsSummaryCard(uiState = uiState)
            }

            item {
                DailyStatsList(uiState = uiState)
            }
        }
    }
}

@Composable
private fun RangeSelector(
    selectedDays: Int,
    onDaysClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Zakres",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 14, 30).forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = { onDaysClick(days) },
                    label = {
                        Text(text = "$days dni")
                    }
                )
            }
        }
    }
}

@Composable
private fun MetricSelector(
    selectedMetric: StatisticsMetric,
    onMetricClick: (StatisticsMetric) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Metryka",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticsMetric.entries.forEach { metric ->
                FilterChip(
                    selected = selectedMetric == metric,
                    onClick = { onMetricClick(metric) },
                    label = {
                        Text(text = metric.label)
                    }
                )
            }
        }
    }
}

@Composable
private fun LineMacroChart(
    days: List<StatisticsDayItem>,
    selectedMetric: StatisticsMetric,
    goal: Double
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val values = days.map { it.valueForMetric(selectedMetric) }

    val maxDataValue = values.maxOrNull() ?: 0.0
    val maxBaseValue = max(maxDataValue, goal).coerceAtLeast(1.0)
    val chartMaxValue = maxBaseValue * 1.15

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val leftPadding = 52.dp.toPx()
        val rightPadding = 12.dp.toPx()
        val topPadding = 16.dp.toPx()
        val bottomPadding = 36.dp.toPx()

        val plotLeft = leftPadding
        val plotRight = size.width - rightPadding
        val plotTop = topPadding
        val plotBottom = size.height - bottomPadding

        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        fun yForValue(value: Double): Float {
            val normalized = (value / chartMaxValue).coerceIn(0.0, 1.0)
            return (plotBottom - normalized.toFloat() * plotHeight)
        }

        fun xForIndex(index: Int): Float {
            if (days.size <= 1) {
                return plotLeft + plotWidth / 2f
            }

            return plotLeft + (index.toFloat() / (days.size - 1).toFloat()) * plotWidth
        }

        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = 10.sp.toPx()
        }

        val gridSteps = 4

        for (i in 0..gridSteps) {
            val value = chartMaxValue * (gridSteps - i) / gridSteps
            val y = plotTop + i * plotHeight / gridSteps

            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.drawText(
                value.formatSmart(0),
                0f,
                y + 4.dp.toPx(),
                labelPaint
            )
        }

        if (goal > 0.0) {
            val goalY = yForValue(goal)

            drawLine(
                color = goalColor,
                start = Offset(plotLeft, goalY),
                end = Offset(plotRight, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(14f, 10f),
                    phase = 0f
                )
            )

            drawContext.canvas.nativeCanvas.drawText(
                "Cel: ${goal.formatSmart(0)}",
                plotLeft,
                goalY - 6.dp.toPx(),
                labelPaint
            )
        }

        if (days.isNotEmpty()) {
            val labelStep = when {
                days.size <= 7 -> 1
                days.size <= 14 -> 2
                else -> 5
            }

            days.forEachIndexed { index, day ->
                if (index % labelStep == 0 || index == days.lastIndex) {
                    val x = xForIndex(index)

                    drawContext.canvas.nativeCanvas.drawText(
                        day.label,
                        x - 14.dp.toPx(),
                        size.height - 10.dp.toPx(),
                        labelPaint
                    )
                }
            }
        }

        val lineStrokeWidth = when {
            days.size <= 7 -> 3.dp.toPx()
            days.size <= 14 -> 2.dp.toPx()
            else -> 1.4.dp.toPx()
        }

        if (days.size >= 2) {
            for (index in 0 until values.lastIndex) {
                val start = Offset(
                    x = xForIndex(index),
                    y = yForValue(values[index])
                )

                val end = Offset(
                    x = xForIndex(index + 1),
                    y = yForValue(values[index + 1])
                )

                drawLine(
                    color = primaryColor,
                    start = start,
                    end = end,
                    strokeWidth = lineStrokeWidth
                )
            }
        }

        //Draw points

        val pointRadius = when {
            days.size <= 7 -> 4.5.dp.toPx()
            days.size <= 14 -> 3.dp.toPx()
            else -> 1.8.dp.toPx()
        }

        val pointOuterRadius = when {
            days.size <= 7 -> 7.dp.toPx()
            days.size <= 14 -> 4.dp.toPx()
            else -> 2.8.dp.toPx()
        }

        val pointStrokeWidth = when {
            days.size <= 7 -> 1.dp.toPx()
            days.size <= 14 -> 0.8.dp.toPx()
            else -> 0.6.dp.toPx()
        }

        values.forEachIndexed { index, value ->
            val point = Offset(
                x = xForIndex(index),
                y = yForValue(value)
            )

            drawCircle(
                color = primaryColor,
                radius = pointRadius,
                center = point
            )

            drawCircle(
                color = primaryColor,
                radius = pointOuterRadius,
                center = point,
                style = Stroke(width = pointStrokeWidth)
            )
        }
    }
}

@Composable
private fun StatisticsSummaryCard(
    uiState: StatisticsUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Podsumowanie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Średnio: ${uiState.averageValue.formatSmart(1)} ${uiState.selectedMetric.unit}"
            )

            Text(
                text = "Najwięcej: ${uiState.highestValue.formatSmart(1)} ${uiState.selectedMetric.unit}"
            )

            Text(
                text = "Najmniej: ${uiState.lowestValue.formatSmart(1)} ${uiState.selectedMetric.unit}"
            )

            Text(
                text = "Cel dzienny: ${uiState.selectedGoal.formatSmart(1)} ${uiState.selectedMetric.unit}"
            )
        }
    }
}

@Composable
private fun DailyStatsList(
    uiState: StatisticsUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Dni",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            uiState.days.reversed().forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = day.label)

                    Text(
                        text = "${day.valueForMetric(uiState.selectedMetric).formatSmart(1)} ${uiState.selectedMetric.unit}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
