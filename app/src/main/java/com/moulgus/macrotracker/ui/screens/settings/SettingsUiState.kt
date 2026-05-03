package com.moulgus.macrotracker.ui.screens.settings

data class SettingsUiState(
    val form: SettingsFormState = SettingsFormState(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)