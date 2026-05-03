package com.moulgus.macrotracker.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import java.util.Locale

@Composable
fun ProductsScreen(
    onBackClick: () -> Unit,
    onAddProductClick: () -> Unit,
    viewModel: ProductsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddProductClick = onAddProductClick,
        onDeleteProductClick = viewModel::deleteProduct
    )
}

@Composable
private fun ProductsScreenContent(
    uiState: ProductsUiState,
    onBackClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit
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
                        text = "Produkty",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(onClick = onBackClick) {
                        Text(text = "Wróć")
                    }
                }
            }

            item {
                Button(
                    onClick = onAddProductClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Dodaj produkt")
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.successMessage != null) {
                item {
                    Text(
                        text = uiState.successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Text(
                    text = "Lista produktów",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = uiState.products,
                key = { it.productID }
            ) { product ->
                ProductListItem(
                    product = product,
                    onDeleteClick = { onDeleteProductClick(product) }
                )
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: ProductEntity,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = if (product.isCustom) "Własny" else "Domyślny",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "${product.kcalPer100.format(0)} kcal / 100 ${product.baseUnit}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "B: ${product.proteinPer100.format(1)} g  W: ${product.carbsPer100.format(1)} g  T: ${product.fatPer100.format(1)} g",
                style = MaterialTheme.typography.bodyMedium
            )

            if (product.isCustom) {
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Usuń produkt")
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(Locale.US, this)
}