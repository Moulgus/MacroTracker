package com.moulgus.macrotracker.ui.screens.productunits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import java.util.Locale

@Composable
fun ProductUnitsScreen(
    productID: Long,
    onBackClick: () -> Unit,
    viewModel: ProductUnitsViewModel = viewModel()
) {
    LaunchedEffect(productID) {
        viewModel.setProductID(productID)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductUnitsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onUnitNameChange = viewModel::changeUnitName,
        onAmountInBaseUnitChange = viewModel::changeAmountInBaseUnit,
        onAddUnitClick = viewModel::addUnit,
        onDeleteUnitClick = viewModel::deleteUnit
    )
}

@Composable
private fun ProductUnitsScreenContent(
    uiState: ProductUnitsUiState,
    onBackClick: () -> Unit,
    onUnitNameChange: (String) -> Unit,
    onAmountInBaseUnitChange: (String) -> Unit,
    onAddUnitClick: () -> Unit,
    onDeleteUnitClick: (ProductUnitEntity) -> Unit
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
                        text = "Jednostki",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(onClick = onBackClick) {
                        Text(text = "Wróć")
                    }
                }
            }

            val product = uiState.product

            if (product == null) {
                item {
                    Text(text = "Ładowanie produktu...")
                }
            } else {
                item {
                    ProductInfoCard(product = product)
                }

                item {
                    AddUnitCard(
                        product = product,
                        unitNameText = uiState.unitNameText,
                        amountInBaseUnitText = uiState.amountInBaseUnitText,
                        errorMessage = uiState.errorMessage,
                        successMessage = uiState.successMessage,
                        onUnitNameChange = onUnitNameChange,
                        onAmountInBaseUnitChange = onAmountInBaseUnitChange,
                        onAddUnitClick = onAddUnitClick
                    )
                }

                item {
                    Text(
                        text = "Dodatkowe jednostki",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.units.isEmpty()) {
                    item {
                        Text(
                            text = "Brak dodatkowych jednostek dla tego produktu.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(
                        items = uiState.units,
                        key = { it.unitID }
                    ) { unit ->
                        UnitListItem(
                            product = product,
                            unit = unit,
                            onDeleteClick = { onDeleteUnitClick(unit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductInfoCard(
    product: ProductEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(text = "Kategoria: ${product.category}")

            Text(
                text = "Jednostka bazowa: ${product.baseUnit}",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Wartości produktu są zapisane dla 100 ${product.baseUnit}."
            )

            HorizontalDivider()

            Text(
                text = "${product.kcalPer100.format(0)} kcal / 100 ${product.baseUnit}"
            )

            Text(
                text = "B: ${product.proteinPer100.format(1)} g  W: ${product.carbsPer100.format(1)} g  T: ${product.fatPer100.format(1)} g"
            )
        }
    }
}

@Composable
private fun AddUnitCard(
    product: ProductEntity,
    unitNameText: String,
    amountInBaseUnitText: String,
    errorMessage: String?,
    successMessage: String?,
    onUnitNameChange: (String) -> Unit,
    onAmountInBaseUnitChange: (String) -> Unit,
    onAddUnitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Dodaj jednostkę",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = unitNameText,
                onValueChange = onUnitNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Nazwa jednostki, np. łyżeczka")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInBaseUnitText,
                onValueChange = onAmountInBaseUnitChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Ile to ${product.baseUnit}, np. 12")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text(
                text = "Przykład: jeśli 1 łyżeczka miodu ma 12 g, wpisujesz nazwę „łyżeczka” i wartość „12”.",
                style = MaterialTheme.typography.bodySmall
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (successMessage != null) {
                Text(
                    text = successMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onAddUnitClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Dodaj jednostkę")
            }
        }
    }
}

@Composable
private fun UnitListItem(
    product: ProductEntity,
    unit: ProductUnitEntity,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = unit.unitName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "1 ${unit.unitName} = ${unit.amountInBaseUnit.format(1)} ${product.baseUnit}"
            )

            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Usuń jednostkę")
            }
        }
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(Locale.US, this)
}