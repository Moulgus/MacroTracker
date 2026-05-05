package com.moulgus.macrotracker.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import androidx.compose.ui.Alignment
import com.moulgus.macrotracker.ui.components.FavoriteStarButton
import com.moulgus.macrotracker.ui.components.ProductCategorySelector
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moulgus.macrotracker.util.formatSmart
import com.moulgus.macrotracker.ui.components.EmptyStateCard

@Composable
fun ProductsScreen(
    onBackClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (Long) -> Unit,
    onProductUnitsClick: (Long) -> Unit,
    viewModel: ProductsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductsScreenContent(
        uiState = uiState,
        onEditProductClick = onEditProductClick,
        onBackClick = onBackClick,
        onSearchQueryChange = viewModel::changeSearchQuery,
        onAddProductClick = onAddProductClick,
        onProductUnitsClick = onProductUnitsClick,
        onCategoryClick = viewModel::selectCategory,
        onFavoriteClick = viewModel::toggleFavorite,
        onDeleteProductClick = viewModel::deleteProduct
    )
}

@Composable
private fun ProductsScreenContent(
    uiState: ProductsUiState,
    onEditProductClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddProductClick: () -> Unit,
    onProductUnitsClick: (Long) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoriteClick: (ProductEntity) -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit
) {
    val productListState = rememberLazyListState()

    var productToDelete by remember {
        mutableStateOf<ProductEntity?>(null)
    }

    if (productToDelete != null) {
        val product = productToDelete!!

        AlertDialog(
            onDismissRequest = {
                productToDelete = null
            },
            title = {
                Text(text = "Usunąć produkt?")
            },
            text = {
                Text(
                    text = "Produkt „${product.name}” zostanie usunięty z listy. Tej operacji nie można cofnąć."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProductClick(product)
                        productToDelete = null
                    }
                ) {
                    Text(text = "Usuń")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        productToDelete = null
                    }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    LaunchedEffect(uiState.searchQuery) {
        productListState.animateScrollToItem(0)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Szukaj produktu")
                },
                singleLine = true
            )

            ProductCategorySelector(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = onCategoryClick
            )

            Button(
                onClick = onAddProductClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Dodaj produkt")
            }

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

            Text(
                text = "Lista produktów",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = productListState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.products.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Brak produktów",
                            message = if (uiState.searchQuery.isBlank() && uiState.selectedCategory == "Wszystkie") {
                                "Nie ma jeszcze żadnych produktów na liście."
                            } else {
                                "Nie znaleziono produktów dla wybranej kategorii lub wpisanej frazy."
                            },
                            actionText = "Dodaj produkt",
                            onActionClick = onAddProductClick
                        )
                    }
                } else {
                    items(
                        items = uiState.products,
                        key = { it.productID }
                    ) { product ->
                        ProductListItem(
                            product = product,
                            onFavoriteClick = { onFavoriteClick(product) },
                            onUnitsClick = { onProductUnitsClick(product.productID) },
                            onEditClick = { onEditProductClick(product.productID) },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: ProductEntity,
    onFavoriteClick: () -> Unit,
    onUnitsClick: () -> Unit,
    onEditClick: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
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
                        text = "${product.kcalPer100.formatSmart(0)} kcal / 100 ${product.baseUnit}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "B: ${product.proteinPer100.formatSmart(1)} g  W: ${product.carbsPer100.formatSmart(1)} g  T: ${product.fatPer100.formatSmart(1)} g",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                FavoriteStarButton(
                    isFavorite = product.isFavorite,
                    onClick = onFavoriteClick
                )
            }

            Text(
                text = "${product.kcalPer100.formatSmart(0)} kcal / 100 ${product.baseUnit}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "B: ${product.proteinPer100.formatSmart(1)} g  W: ${product.carbsPer100.formatSmart(1)} g  T: ${product.fatPer100.formatSmart(1)} g",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUnitsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Jednostki")
                }

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Edytuj")
                }
            }

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