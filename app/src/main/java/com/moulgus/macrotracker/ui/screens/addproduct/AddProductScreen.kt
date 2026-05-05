package com.moulgus.macrotracker.ui.screens.addproduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.moulgus.macrotracker.ui.screens.products.ProductFormState
import com.moulgus.macrotracker.ui.screens.products.ProductsViewModel
import androidx.compose.runtime.LaunchedEffect
import com.moulgus.macrotracker.ui.components.BackHeader

@Composable
fun AddProductScreen(
    onBackClick: () -> Unit,
    editProductID: Long? = null,
    viewModel: ProductsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(editProductID) {
        viewModel.setEditProductID(editProductID)
    }

    AddProductScreenContent(
        form = uiState.form,
        isEditMode = uiState.isEditMode,
        errorMessage = uiState.errorMessage,
        onBackClick = onBackClick,
        onNameChange = viewModel::changeName,
        onCategoryChange = viewModel::changeCategory,
        onBaseUnitChange = viewModel::changeBaseUnit,
        onKcalChange = viewModel::changeKcal,
        onProteinChange = viewModel::changeProtein,
        onCarbsChange = viewModel::changeCarbs,
        onFatChange = viewModel::changeFat,
        onAddProductClick = {
            viewModel.saveProduct(
                onSuccess = onBackClick
            )
        }
    )
}

@Composable
private fun AddProductScreenContent(
    form: ProductFormState,
    isEditMode: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onBaseUnitChange: (String) -> Unit,
    onKcalChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onAddProductClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackHeader(
                title = if (isEditMode) "Edytuj produkt" else "Dodaj produkt",
                onBackClick = onBackClick
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = form.nameText,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Nazwa produktu") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = form.categoryText,
                        onValueChange = onCategoryChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Kategoria") },
                        singleLine = true
                    )

                    Text(
                        text = "Jednostka bazowa",
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = form.baseUnit == "g",
                            onClick = { onBaseUnitChange("g") },
                            label = { Text(text = "g") }
                        )

                        FilterChip(
                            selected = form.baseUnit == "ml",
                            onClick = { onBaseUnitChange("ml") },
                            label = { Text(text = "ml") }
                        )
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = form.kcalText,
                        onValueChange = onKcalChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Kcal / 100 ${form.baseUnit}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = form.proteinText,
                        onValueChange = onProteinChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Białko / 100 ${form.baseUnit}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = form.carbsText,
                        onValueChange = onCarbsChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Węgle / 100 ${form.baseUnit}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = form.fatText,
                        onValueChange = onFatChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Tłuszcz / 100 ${form.baseUnit}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onAddProductClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isEditMode) {
                                "Zapisz zmiany"
                            } else {
                                "Zapisz produkt"
                            }
                        )
                    }
                }
            }
        }
    }
}