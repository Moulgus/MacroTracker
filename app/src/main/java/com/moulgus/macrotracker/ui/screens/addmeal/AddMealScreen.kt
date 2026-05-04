package com.moulgus.macrotracker.ui.screens.addmeal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import java.util.Locale
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import com.moulgus.macrotracker.data.local.model.MealTemplateWithEntries
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.navigationBarsPadding
import com.moulgus.macrotracker.ui.components.FavoriteStarButton
import com.moulgus.macrotracker.ui.components.ProductCategorySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBackClick: () -> Unit,
    editMealID: Long? = null,
    viewModel: AddMealViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(editMealID) {
        viewModel.setEditMealID(editMealID)
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )

    val targetSheetPeekHeight = if (uiState.selectedProduct == null) {
        390.dp
    } else {
        440.dp
    }

    val sheetPeekHeight by animateDpAsState(
        targetValue = targetSheetPeekHeight,
        label = "sheetPeekHeight"
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    AddMealScreenContent(
        uiState = uiState,
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        onProductClick = viewModel::selectProduct,
        onToggleTemplatesClick = viewModel::toggleTemplates,
        onUseTemplateClick = viewModel::addTemplateToCurrentMeal,
        onDeleteTemplateClick = viewModel::deleteTemplate,
        onSaveTemplateClick = viewModel::saveCurrentMealAsTemplate,
        onSearchQueryChange = viewModel::changeSearchQuery,
        onMealNameChange = viewModel::changeMealName,
        onMoveDateBackClick = viewModel::moveSelectedDateBack,
        onMoveDateForwardClick = viewModel::moveSelectedDateForward,
        onTodayDateClick = viewModel::selectCurrentTrackingDate,
        onAmountChange = viewModel::changeAmount,
        onUnitClick = viewModel::selectUnit,
        onAddIngredientClick = viewModel::addSelectedIngredient,
        onRemoveIngredientClick = viewModel::removeIngredient,
        onCategoryClick = viewModel::selectCategory,
        onFavoriteClick = viewModel::toggleFavorite,
        onBackClick = onBackClick,
        onDismissTemplateSavedPopup = viewModel::dismissTemplateSavedPopup,
        onSaveMealClick = {
            viewModel.saveMeal(
                onSuccess = onBackClick
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealScreenContent(
    uiState: AddMealUiState,
    scaffoldState: androidx.compose.material3.BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    onProductClick: (ProductEntity) -> Unit,
    onToggleTemplatesClick: () -> Unit,
    onUseTemplateClick: (Long) -> Unit,
    onDeleteTemplateClick: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onMealNameChange: (String) -> Unit,
    onMoveDateBackClick: () -> Unit,
    onMoveDateForwardClick: () -> Unit,
    onTodayDateClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onUnitClick: (String) -> Unit,
    onAddIngredientClick: () -> Unit,
    onRemoveIngredientClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onDismissTemplateSavedPopup: () -> Unit,
    onSaveMealClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoriteClick: (ProductEntity) -> Unit,
    onSaveTemplateClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isSheetHidden = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
    val productListState = rememberLazyListState()

    LaunchedEffect(uiState.searchQuery) {
        if (uiState.products.isNotEmpty()) {
            productListState.animateScrollToItem(0)
        }
    }

    if (uiState.templateSavedMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissTemplateSavedPopup,
            title = {
                Text(text = "Szablon zapisany")
            },
            text = {
                Text(text = uiState.templateSavedMessage)
            },
            confirmButton = {
                Button(onClick = onDismissTemplateSavedPopup) {
                    Text(text = "OK")
                }
            }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        sheetContent = {
            AddMealBottomSheetContent(
                uiState = uiState,
                onMealNameChange = onMealNameChange,
                onMoveDateBackClick = onMoveDateBackClick,
                onMoveDateForwardClick = onMoveDateForwardClick,
                onTodayDateClick = onTodayDateClick,
                onAmountChange = onAmountChange,
                onUnitClick = onUnitClick,
                onAddIngredientClick = onAddIngredientClick,
                onRemoveIngredientClick = onRemoveIngredientClick,
                onSaveMealClick = onSaveMealClick,
                onSaveTemplateClick = onSaveTemplateClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.isEditMode) "Edytuj posiłek" else "Dodaj posiłek",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(onClick = onBackClick) {
                        Text(text = "Wróć")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(2f),
                        label = {
                            Text(text = "Szukaj")
                        },
                        singleLine = true
                    )

                    Button(
                        onClick = onToggleTemplatesClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (uiState.showTemplates) {
                                "Produkty"
                            } else {
                                "Szablony"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!uiState.showTemplates) {
                    ProductCategorySelector(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategoryClick = onCategoryClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = if (uiState.showTemplates) "Szablony" else "Produkt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = productListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        bottom = if (isSheetHidden) 72.dp else 410.dp
                    )
                ) {
                    if (uiState.showTemplates) {
                        if (uiState.templates.isEmpty()) {
                            item {
                                Text(
                                    text = "Brak szablonów.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(
                                items = uiState.templates,
                                key = { it.template.templateID }
                            ) { template ->
                                TemplateItem(
                                    template = template,
                                    onUseClick = {
                                        onUseTemplateClick(template.template.templateID)
                                    },
                                    onDeleteClick = {
                                        onDeleteTemplateClick(template.template.templateID)
                                    }
                                )
                            }
                        }
                    } else {
                        if (uiState.products.isEmpty()) {
                            item {
                                Text(
                                    text = "Nie znaleziono produktu.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(
                                items = uiState.products,
                                key = { it.productID }
                            ) { product ->
                                ProductItem(
                                    product = product,
                                    isSelected = uiState.selectedProduct?.productID == product.productID,
                                    onClick = {
                                        onProductClick(product)

                                        coroutineScope.launch {
                                            scaffoldState.bottomSheetState.show()
                                        }
                                    },
                                    onFavoriteClick = {
                                        onFavoriteClick(product)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isSheetHidden) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(text = "Pokaż panel dodawania")
                }
            }
        }
    }
}

        @Composable
        private fun AddMealBottomSheetContent(
            uiState: AddMealUiState,
            onMealNameChange: (String) -> Unit,
            onMoveDateBackClick: () -> Unit,
            onMoveDateForwardClick: () -> Unit,
            onTodayDateClick: () -> Unit,
            onAmountChange: (String) -> Unit,
            onUnitClick: (String) -> Unit,
            onAddIngredientClick: () -> Unit,
            onRemoveIngredientClick: (Long) -> Unit,
            onSaveMealClick: () -> Unit,
            onSaveTemplateClick: () -> Unit,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (uiState.selectedProduct != null) {
                        "Wybrano: ${uiState.selectedProduct.name}"
                    } else {
                        "Wybierz produkt z listy"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Ilość")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                UnitSelector(
                    uiState = uiState,
                    onUnitClick = onUnitClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                CalculatedMacroPreview(uiState = uiState)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAddIngredientClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Dodaj składnik do posiłku")
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.mealNameText,
                    onValueChange = onMealNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Nazwa posiłku opcjonalnie")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                DateSelector(
                    selectedDateLabel = uiState.selectedDateLabel,
                    canMoveToNextDay = uiState.canMoveToNextDay,
                    onMoveDateBackClick = onMoveDateBackClick,
                    onMoveDateForwardClick = onMoveDateForwardClick,
                    onTodayDateClick = onTodayDateClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                DraftIngredientsCard(
                    ingredients = uiState.ingredients,
                    totalKcal = uiState.totalKcal,
                    totalProtein = uiState.totalProtein,
                    totalCarbs = uiState.totalCarbs,
                    totalFat = uiState.totalFat,
                    onRemoveIngredientClick = onRemoveIngredientClick
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSaveTemplateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Zapisz jako szablon")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSaveMealClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isEditMode) {
                            "Zapisz zmiany"
                        } else {
                            "Zapisz posiłek"
                        }
                    )
                }
            }
        }

        @Composable
        private fun DateSelector(
            selectedDateLabel: String,
            canMoveToNextDay: Boolean,
            onMoveDateBackClick: () -> Unit,
            onMoveDateForwardClick: () -> Unit,
            onTodayDateClick: () -> Unit
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Data posiłku",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMoveDateBackClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "← dzień")
                    }

                    Button(
                        onClick = onMoveDateForwardClick,
                        enabled = canMoveToNextDay,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "dzień →")
                    }
                }

                Text(
                    text = selectedDateLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = onTodayDateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Ustaw na dzisiaj")
                }
            }
        }

        @Composable
        private fun ProductItem(
            product: ProductEntity,
            isSelected: Boolean,
            onClick: () -> Unit,
            onFavoriteClick: () -> Unit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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

                    FavoriteStarButton(
                        isFavorite = product.isFavorite,
                        onClick = onFavoriteClick
                    )
                }
            }
        }

        @Composable
        private fun TemplateItem(
            template: MealTemplateWithEntries,
            onUseClick: () -> Unit,
            onDeleteClick: () -> Unit
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = template.template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    template.entries.forEach { entry ->
                        Text(
                            text = "${entry.productName} — ${entry.amount.format(1)} ${entry.unitName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = "${template.kcal.format(0)} kcal | B: ${template.protein.format(1)} W: ${
                            template.carbs.format(
                                1
                            )
                        } T: ${template.fat.format(1)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onUseClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Użyj")
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
                        text = "Wyliczenie składnika",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Kalorie: ${uiState.calculatedKcal.format(0)} kcal")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "B: ${uiState.calculatedProtein.format(1)} g  W: ${
                            uiState.calculatedCarbs.format(
                                1
                            )
                        } g  T: ${uiState.calculatedFat.format(1)} g"
                    )
                }
            }
        }

        @Composable
        private fun DraftIngredientsCard(
            ingredients: List<DraftMealIngredient>,
            totalKcal: Double,
            totalProtein: Double,
            totalCarbs: Double,
            totalFat: Double,
            onRemoveIngredientClick: (Long) -> Unit
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Składniki posiłku",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (ingredients.isEmpty()) {
                        Text(text = "Brak składników.")
                    } else {
                        ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${ingredient.productName} — ${
                                            ingredient.amount.format(
                                                1
                                            )
                                        } ${ingredient.unitName}",
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "${ingredient.kcal.format(0)} kcal | B: ${
                                            ingredient.protein.format(
                                                1
                                            )
                                        } W: ${ingredient.carbs.format(1)} T: ${
                                            ingredient.fat.format(
                                                1
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        onRemoveIngredientClick(ingredient.draftIngredientID)
                                    }
                                ) {
                                    Text(text = "Usuń")
                                }
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "Razem: ${totalKcal.format(0)} kcal",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "B: ${totalProtein.format(1)} g  W: ${totalCarbs.format(1)} g  T: ${
                                totalFat.format(
                                    1
                                )
                            } g"
                        )
                    }
                }
            }
        }

        private fun Double.format(decimals: Int): String {
            return "%.${decimals}f".format(Locale.US, this)
        }
