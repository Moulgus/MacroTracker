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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moulgus.macrotracker.util.formatSmart
import com.moulgus.macrotracker.ui.components.EmptyStateCard
import androidx.compose.foundation.ScrollState
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.itemsIndexed
import com.moulgus.macrotracker.ui.components.BackHeader
import com.moulgus.macrotracker.R
import com.moulgus.macrotracker.ui.components.SmallActionIconButton

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
        initialValue = SheetValue.Hidden,
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

    val bottomSheetScrollState = rememberScrollState()

    var templateToDelete by remember {
        mutableStateOf<MealTemplateWithEntries?>(null)
    }

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
    if (templateToDelete != null) {
        val template = templateToDelete!!

        AlertDialog(
            onDismissRequest = {
                templateToDelete = null
            },
            title = {
                Text(text = "Usunąć szablon?")
            },
            text = {
                Text(
                    text = "Szablon „${template.template.name}” zostanie usunięty. Tej operacji nie można cofnąć."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTemplateClick(template.template.templateID)
                        templateToDelete = null
                    }
                ) {
                    Text(text = "Usuń")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        templateToDelete = null
                    }
                ) {
                    Text(text = "Anuluj")
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
                scrollState = bottomSheetScrollState,
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
                BackHeader(
                    title = if (uiState.isEditMode) "Edytuj posiłek" else "Dodaj posiłek",
                    onBackClick = onBackClick
                )

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
                                EmptyStateCard(
                                    title = "Brak szablonów",
                                    message = if (uiState.searchQuery.isBlank()) {
                                        "Nie masz jeszcze zapisanych szablonów. Ułóż posiłek, wpisz jego nazwę i zapisz go jako szablon."
                                    } else {
                                        "Nie znaleziono szablonu pasującego do wpisanej frazy."
                                    }
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

                                        coroutineScope.launch {
                                            scaffoldState.bottomSheetState.expand()

                                            delay(200L)

                                            bottomSheetScrollState.animateScrollTo(
                                                bottomSheetScrollState.maxValue
                                            )
                                        }
                                    },
                                    onDeleteClick = {
                                        templateToDelete = template
                                    }
                                )
                            }
                        }
                    } else {
                        if (uiState.products.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "Nie znaleziono produktu",
                                    message = if (uiState.searchQuery.isBlank() && uiState.selectedCategory == "Wszystkie") {
                                        "Lista produktów jest pusta. Dodaj produkt w ekranie Produkty."
                                    } else {
                                        "Nie ma produktu pasującego do wybranej kategorii lub wpisanej frazy."
                                    }
                                )
                            }
                        } else {
                            itemsIndexed(
                                items = uiState.products,
                                key = { _, product -> product.productID }
                            ) { index, product ->
                                ProductItem(
                                    product = product,
                                    isSelected = uiState.selectedProduct?.productID == product.productID,
                                    onClick = {
                                        onProductClick(product)

                                        coroutineScope.launch {
                                            scaffoldState.bottomSheetState.show()

                                            productListState.animateScrollToItem(
                                                index = index
                                            )
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
            scrollState: ScrollState,
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
                    .verticalScroll(scrollState)
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
                    singleLine = true,
                    isError = uiState.ingredientErrorMessage != null
                )
                if (uiState.selectedProduct == null && uiState.ingredientErrorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = uiState.ingredientErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallActionIconButton(
                        iconResID = R.drawable.ic_arrow_previous,
                        contentDescription = "Poprzedni dzień",
                        onClick = onMoveDateBackClick,
                        buttonSize = 45.dp,
                        iconSize = 36.dp
                    )

                    SmallActionIconButton(
                        iconResID = R.drawable.ic_arrow_next,
                        contentDescription = "Następny dzień",
                        onClick = onMoveDateForwardClick,
                        enabled = canMoveToNextDay,
                        buttonSize = 45.dp,
                        iconSize = 36.dp
                    )
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
                            text = "${product.kcalPer100.formatSmart(0)} kcal / 100 ${product.baseUnit}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "B: ${product.proteinPer100.formatSmart(1)} g  W: ${product.carbsPer100.formatSmart(1)} g  T: ${product.fatPer100.formatSmart(1)} g",
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
                            text = "${entry.productName} — ${entry.amount.formatSmart(1)} ${entry.unitName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = "${template.kcal.formatSmart(0)} kcal | B: ${template.protein.formatSmart(1)} W: ${
                            template.carbs.formatSmart(
                                1
                            )
                        } T: ${template.fat.formatSmart(1)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onUseClick
                        ) {
                            Text(text = "Użyj")
                        }

                        SmallActionIconButton(
                            iconResID = R.drawable.ic_delete,
                            contentDescription = "Usuń szablon",
                            onClick = onDeleteClick,
                            tint = MaterialTheme.colorScheme.error
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jednostka",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.ingredientErrorMessage != null) {
                    Text(
                        text = uiState.ingredientErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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

                    Text(text = "Kalorie: ${uiState.calculatedKcal.formatSmart(0)} kcal")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "B: ${uiState.calculatedProtein.formatSmart(1)} g  W: ${
                            uiState.calculatedCarbs.formatSmart(
                                1
                            )
                        } g  T: ${uiState.calculatedFat.formatSmart(1)} g"
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
                        EmptyStateCard(
                            title = "Brak składników",
                            message = "Wybierz produkt z listy, wpisz ilość i kliknij „Dodaj składnik do posiłku”."
                        )
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
                                            ingredient.amount.formatSmart(
                                                1
                                            )
                                        } ${ingredient.unitName}",
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "${ingredient.kcal.formatSmart(0)} kcal | B: ${
                                            ingredient.protein.formatSmart(
                                                1
                                            )
                                        } W: ${ingredient.carbs.formatSmart(1)} T: ${
                                            ingredient.fat.formatSmart(
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
                            text = "Razem: ${totalKcal.formatSmart(0)} kcal",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "B: ${totalProtein.formatSmart(1)} g  W: ${totalCarbs.formatSmart(1)} g  T: ${
                                totalFat.formatSmart(
                                    1
                                )
                            } g"
                        )
                    }
                }
            }
        }

