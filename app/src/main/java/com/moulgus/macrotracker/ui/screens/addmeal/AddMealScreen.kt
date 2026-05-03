package com.moulgus.macrotracker.ui.screens.addmeal

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import java.util.Locale

@Composable
fun AddMealScreen(
    onBackClick: () -> Unit,
    viewModel: AddMealViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddMealScreenContent(
        uiState = uiState,
        onProductClick = viewModel::selectProduct,
        onAmountChange = viewModel::changeAmount,
        onUnitClick = viewModel::selectUnit,
        onBackClick = onBackClick,
        onAddClick = {
            viewModel.addMealEntry(
                onSuccess = onBackClick
            )
        }
    )
}

@Composable
private fun AddMealScreenContent(
    uiState: AddMealUiState,
    onProductClick: (ProductEntity) -> Unit,
    onAmountChange: (String) -> Unit,
    onUnitClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dodaj posiłek",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onBackClick) {
                    Text(text = "Wróć")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Produkt",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.products,
                    key = { it.productID }
                ) { product ->
                    ProductItem(
                        product = product,
                        isSelected = uiState.selectedProduct?.productID == product.productID,
                        onClick = { onProductClick(product) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Ilość")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            UnitSelector(
                uiState = uiState,
                onUnitClick = onUnitClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalculatedMacroPreview(uiState = uiState)

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Dodaj do dzisiaj")
            }
        }
    }
}

@Composable
private fun ProductItem(
    product: ProductEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${product.kcalPer100.format(0)} kcal / 100 ${product.baseUnit}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "B: ${product.proteinPer100.format(1)} g  W: ${product.carbsPer100.format(1)} g  T: ${product.fatPer100.format(1)} g",
                style = MaterialTheme.typography.bodySmall
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Wybrano",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UnitSelector(
    uiState: AddMealUiState,
    onUnitClick: (String) -> Unit
) {
    val product = uiState.selectedProduct ?: return

    val unitNames = buildList {
        add(product.baseUnit)
        addAll(uiState.productUnits.map { it.unitName })
    }

    Text(
        text = "Jednostka",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        unitNames.forEach { unitName ->
            FilterChip(
                selected = uiState.selectedUnitName == unitName,
                onClick = { onUnitClick(unitName) },
                label = {
                    Text(text = unitName)
                }
            )
        }
    }
}

@Composable
private fun CalculatedMacroPreview(
    uiState: AddMealUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Wyliczenie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Kalorie: ${uiState.calculatedKcal.format(0)} kcal")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "B: ${uiState.calculatedProtein.format(1)} g  W: ${uiState.calculatedCarbs.format(1)} g  T: ${uiState.calculatedFat.format(1)} g"
            )
        }
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(Locale.US, this)
}