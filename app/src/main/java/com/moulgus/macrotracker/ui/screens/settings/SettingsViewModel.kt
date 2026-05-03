package com.moulgus.macrotracker.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userSettingsRepository =
        (application as MacroTrackerApplication).userSettingsRepository

    private val formState = MutableStateFlow(SettingsFormState())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    private var hasLoadedInitialSettings = false

    init {
        viewModelScope.launch {
            userSettingsRepository.goalsFlow.collect { goals ->
                if (!hasLoadedInitialSettings) {
                    formState.value = SettingsFormState(
                        kcalGoalText = goals.kcalGoal.toInputText(),
                        proteinGoalText = goals.proteinGoal.toInputText(),
                        carbsGoalText = goals.carbsGoal.toInputText(),
                        fatGoalText = goals.fatGoal.toInputText()
                    )

                    hasLoadedInitialSettings = true
                }
            }
        }
    }

    val uiState = combine(
        formState,
        errorMessage,
        successMessage
    ) { form, error, success ->
        SettingsUiState(
            form = form,
            errorMessage = error,
            successMessage = success
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun changeKcalGoal(value: String) {
        formState.value = formState.value.copy(
            kcalGoalText = normalizeDecimalInput(value)
        )
        clearMessages()
    }

    fun changeProteinGoal(value: String) {
        formState.value = formState.value.copy(
            proteinGoalText = normalizeDecimalInput(value)
        )
        clearMessages()
    }

    fun changeCarbsGoal(value: String) {
        formState.value = formState.value.copy(
            carbsGoalText = normalizeDecimalInput(value)
        )
        clearMessages()
    }

    fun changeFatGoal(value: String) {
        formState.value = formState.value.copy(
            fatGoalText = normalizeDecimalInput(value)
        )
        clearMessages()
    }

    fun saveSettings() {
        val form = formState.value

        val kcalGoal = form.kcalGoalText.toDoubleOrNull()
        val proteinGoal = form.proteinGoalText.toDoubleOrNull()
        val carbsGoal = form.carbsGoalText.toDoubleOrNull()
        val fatGoal = form.fatGoalText.toDoubleOrNull()

        if (kcalGoal == null || kcalGoal <= 0.0) {
            errorMessage.value = "Wpisz poprawny dzienny limit kcal."
            return
        }

        if (proteinGoal == null || proteinGoal < 0.0) {
            errorMessage.value = "Wpisz poprawny cel białka."
            return
        }

        if (carbsGoal == null || carbsGoal < 0.0) {
            errorMessage.value = "Wpisz poprawny cel węgli."
            return
        }

        if (fatGoal == null || fatGoal < 0.0) {
            errorMessage.value = "Wpisz poprawny cel tłuszczu."
            return
        }

        viewModelScope.launch {
            userSettingsRepository.updateGoals(
                kcalGoal = kcalGoal,
                proteinGoal = proteinGoal,
                carbsGoal = carbsGoal,
                fatGoal = fatGoal
            )

            errorMessage.value = null
            successMessage.value = "Zapisano cele."
        }
    }

    private fun normalizeDecimalInput(value: String): String {
        return value.replace(",", ".")
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }

    private fun Double.toInputText(): String {
        return if (this % 1.0 == 0.0) {
            this.toLong().toString()
        } else {
            this.toString()
        }
    }
}