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
import com.moulgus.macrotracker.data.local.model.MealWithEntries
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

@Composable
fun TodayScreen(
    onAddMealClick: () -> Unit,
    onProductsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onEditMealClick: (Long) -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncCurrentTrackingDateIfFollowing()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    TodayScreenContent(
        uiState = uiState,
        onDeleteMealClick = { meal ->
            viewModel.deleteMeal(meal.meal.mealID)
        },
        onEditMealClick = onEditMealClick,
        onMoveDateBackClick = viewModel::moveSelectedDateBack,
        onMoveDateForwardClick = viewModel::moveSelectedDateForward,
        onTodayDateClick = viewModel::selectCurrentTrackingDate,
        onAddMealClick = onAddMealClick,
        onProductsClick = onProductsClick,
        onSettingsClick = onSettingsClick,
        onStatisticsClick = onStatisticsClick
    )
}

@Composable
private fun TodayScreenContent(
    uiState: TodayUiState,
    onDeleteMealClick: (MealWithEntries) -> Unit,
    onEditMealClick: (Long) -> Unit,
    onMoveDateBackClick: () -> Unit,
    onMoveDateForwardClick: () -> Unit,
    onTodayDateClick: () -> Unit,
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
            TodayHeader(
                title = if (uiState.isCurrentTrackingDate) "Dzisiaj" else "Wybrany dzień",
                selectedDateLabel = uiState.dateLabel,
                canMoveToNextDay = uiState.canMoveToNextDay,
                onSettingsClick = onSettingsClick,
                onMoveDateBackClick = onMoveDateBackClick,
                onMoveDateForwardClick = onMoveDateForwardClick,
                onTodayDateClick = onTodayDateClick
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dzisiejsze posiłki",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.meals.isEmpty()) {
                Text(
                    text = "Nie dodano jeszcze żadnych posiłków dzisiaj.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.meals,
                        key = { it.meal.mealID }
                    ) { meal ->
                        MealItem(
                            meal = meal,
                            onEditClick = {
                                onEditMealClick(meal.meal.mealID)
                            },
                            onDeleteClick = {
                                onDeleteMealClick(meal)
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun TodayHeader(
    title: String,
    selectedDateLabel: String,
    canMoveToNextDay: Boolean,
    onSettingsClick: () -> Unit,
    onMoveDateBackClick: () -> Unit,
    onMoveDateForwardClick: () -> Unit,
    onTodayDateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onSettingsClick,
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "⚙",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = selectedDateLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMoveDateBackClick,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "←")
                }

                Button(
                    onClick = onMoveDateForwardClick,
                    enabled = canMoveToNextDay,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "→")
                }

                Button(
                    onClick = onTodayDateClick,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Dzisiaj",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
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
private fun MealItem(
    meal: MealWithEntries,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (!meal.meal.name.isNullOrBlank()) {
                Text(
                    text = meal.meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    meal.entries.forEach { entry ->
                        Text(
                            text = "${entry.productName} — ${entry.amount.format(1)} ${entry.unitName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "Kcal: ${meal.kcal.format(0)}",
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = "B: ${meal.protein.format(1)} g")
                    Text(text = "W: ${meal.carbs.format(1)} g")
                    Text(text = "T: ${meal.fat.format(1)} g")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Edytuj")
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Usuń")
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(Locale.US, this)
}