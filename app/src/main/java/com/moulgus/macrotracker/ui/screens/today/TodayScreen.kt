package com.moulgus.macrotracker.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moulgus.macrotracker.data.local.entity.MealEntryEntity
import java.util.Locale

@Composable
fun TodayScreen(
    onAddMealClick: () -> Unit,
    onProductsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayScreenContent(
        uiState = uiState,
        onDeleteEntry = viewModel::deleteEntry,
        onAddMealClick = onAddMealClick,
        onProductsClick = onProductsClick,
        onSettingsClick = onSettingsClick,
        onStatisticsClick = onStatisticsClick
    )
}

@Composable
private fun TodayScreenContent(
    uiState: TodayUiState,
    onDeleteEntry: (MealEntryEntity) -> Unit,
    onAddMealClick: () -> Unit,
    onProductsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Dzisiaj",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = uiState.date,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            DailySummaryCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddMealClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Dodaj posiłek")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onProductsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Produkty")
                }

                Button(
                    onClick = onStatisticsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Statystyki")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ustawienia")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dzisiejsze wpisy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.entries.isEmpty()) {
                Text(
                    text = "Nie dodano jeszcze żadnych produktów dzisiaj.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.entries,
                        key = { it.entryID }
                    ) { entry ->
                        MealEntryItem(
                            entry = entry,
                            onDeleteClick = { onDeleteEntry(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    uiState: TodayUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Podsumowanie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MacroProgressRow(
                label = "Kalorie",
                eaten = uiState.eatenKcal,
                goal = uiState.kcalGoal,
                unit = "kcal",
                decimals = 0
            )

            MacroProgressRow(
                label = "Białko",
                eaten = uiState.eatenProtein,
                goal = uiState.proteinGoal,
                unit = "g",
                decimals = 1
            )

            MacroProgressRow(
                label = "Węgle",
                eaten = uiState.eatenCarbs,
                goal = uiState.carbsGoal,
                unit = "g",
                decimals = 1
            )

            MacroProgressRow(
                label = "Tłuszcz",
                eaten = uiState.eatenFat,
                goal = uiState.fatGoal,
                unit = "g",
                decimals = 1
            )
        }
    }
}

@Composable
private fun MacroProgressRow(
    label: String,
    eaten: Double,
    goal: Double,
    unit: String,
    decimals: Int
) {
    val progress = if (goal > 0.0) {
        (eaten / goal).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val remaining = (goal - eaten).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${eaten.format(decimals)} / ${goal.format(decimals)} $unit"
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Pozostało: ${remaining.format(decimals)} $unit",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MealEntryItem(
    entry: MealEntryEntity,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = entry.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${entry.amount.format(1)} ${entry.unitName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "${entry.kcal.format(0)} kcal",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "B: ${entry.protein.format(1)} g  W: ${entry.carbs.format(1)} g  T: ${entry.fat.format(1)} g",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Usuń")
            }
        }
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(Locale.US, this)
}