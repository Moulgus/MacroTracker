package com.moulgus.macrotracker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moulgus.macrotracker.ui.components.BackHeader

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showImportConfirmDialog by remember {
        mutableStateOf(false)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
            },
            title = {
                Text(text = "Zaimportować dane?")
            },
            text = {
                Text(
                    text = "Import zastąpi obecne produkty, posiłki, szablony, jednostki i ustawienia. Tej operacji nie można cofnąć."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        importLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/*",
                                "*/*"
                            )
                        )
                    }
                ) {
                    Text(text = "Importuj")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                    }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    SettingsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onKcalGoalChange = viewModel::changeKcalGoal,
        onProteinGoalChange = viewModel::changeProteinGoal,
        onCarbsGoalChange = viewModel::changeCarbsGoal,
        onFatGoalChange = viewModel::changeFatGoal,
        onSaveClick = viewModel::saveSettings,
        onExportBackupClick = {
            exportLauncher.launch("macro_tracker_backup_${System.currentTimeMillis()}.json")
        },
        onImportBackupClick = {
            showImportConfirmDialog = true
        }
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onKcalGoalChange: (String) -> Unit,
    onProteinGoalChange: (String) -> Unit,
    onCarbsGoalChange: (String) -> Unit,
    onFatGoalChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onExportBackupClick: () -> Unit,
    onImportBackupClick: () -> Unit
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BackHeader(
                    title = "Ustawienia",
                    onBackClick = onBackClick
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Dzienne cele",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = uiState.form.kcalGoalText,
                            onValueChange = onKcalGoalChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Limit kcal") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        OutlinedTextField(
                            value = uiState.form.proteinGoalText,
                            onValueChange = onProteinGoalChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Białko dziennie (g)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        OutlinedTextField(
                            value = uiState.form.carbsGoalText,
                            onValueChange = onCarbsGoalChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Węgle dziennie (g)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        OutlinedTextField(
                            value = uiState.form.fatGoalText,
                            onValueChange = onFatGoalChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Tłuszcz dziennie (g)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (uiState.successMessage != null) {
                            Text(
                                text = uiState.successMessage,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Zapisz cele")
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Backup danych",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Eksport zapisuje produkty, posiłki, jednostki, szablony, ulubione i cele do pliku JSON.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = onExportBackupClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Eksportuj dane")
                        }

                        Button(
                            onClick = onImportBackupClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Importuj dane")
                        }
                    }
                }
            }
        }
    }
}