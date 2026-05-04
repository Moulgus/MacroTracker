package com.moulgus.macrotracker.ui.screens.addmeal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import com.moulgus.macrotracker.util.TrackingDateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.moulgus.macrotracker.widget.MacroTrackerWidgetUpdater
import com.moulgus.macrotracker.data.local.model.MealTemplateWithEntries

@OptIn(ExperimentalCoroutinesApi::class)
class AddMealViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as MacroTrackerApplication

    private val repository = app.repository

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val labelFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val selectedProductID = MutableStateFlow<Long?>(null)
    private val editMealID = MutableStateFlow<Long?>(null)
    private val showTemplates = MutableStateFlow(false)
    private val searchQuery = MutableStateFlow("")
    private val mealNameText = MutableStateFlow("")
    private val selectedDate = MutableStateFlow(TrackingDateUtils.getCurrentTrackingDate())
    private val amountText = MutableStateFlow("")
    private val selectedUnitName = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val ingredients = MutableStateFlow<List<DraftMealIngredient>>(emptyList())
    private val templateSavedMessage = MutableStateFlow<String?>(null)
    private var nextDraftIngredientID = 1L
    private val categoryAll = "Wszystkie"
    private val categoryFavorites = "Ulubione"
    private val selectedCategory = MutableStateFlow(categoryAll)
    private var loadedEditMealID: Long? = null

    private val productsFlow = repository.observeAllProducts()

    private val templatesFlow = repository.observeMealTemplatesWithEntries()

    private val filteredTemplatesFlow = combine(
        templatesFlow,
        searchQuery
    ) { templates: List<MealTemplateWithEntries>, query: String ->
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            templates
        } else {
            templates.filter { template ->
                template.template.name.contains(cleanQuery, ignoreCase = true) ||
                        template.entries.any { entry ->
                            entry.productName.contains(cleanQuery, ignoreCase = true)
                        }
            }
        }
    }

    private val templateStateFlow = combine(
        filteredTemplatesFlow,
        showTemplates,
        templateSavedMessage
    ) { templates: List<MealTemplateWithEntries>, show: Boolean, message: String? ->
        TemplateState(
            templates = templates,
            showTemplates = show,
            templateSavedMessage = message
        )
    }

    private val filteredProductsFlow = combine(
        productsFlow,
        searchQuery,
        selectedCategory
    ) { products: List<ProductEntity>, query: String, category: String ->
        val cleanQuery = query.trim()

        products
            .filter { product ->
                when (category) {
                    categoryAll -> true
                    categoryFavorites -> product.isFavorite
                    else -> product.category.equals(category, ignoreCase = true)
                }
            }
            .filter { product ->
                if (cleanQuery.isBlank()) {
                    true
                } else {
                    product.name.contains(cleanQuery, ignoreCase = true) ||
                            product.category.contains(cleanQuery, ignoreCase = true)
                }
            }
    }
    private val categoryStateFlow = combine(
        productsFlow,
        selectedCategory
    ) { products: List<ProductEntity>, selected: String ->
        val productCategories = products
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        CategoryState(
            categories = listOf(categoryAll, categoryFavorites) + productCategories,
            selectedCategory = selected
        )
    }

    private val selectedProductFlow = combine(
        productsFlow,
        selectedProductID
    ) { products: List<ProductEntity>, productID: Long? ->
        products.firstOrNull { it.productID == productID }
    }

    private val productUnitsFlow = selectedProductID.flatMapLatest { productID ->
        if (productID == null) {
            flowOf(emptyList())
        } else {
            repository.observeUnitsForProduct(productID)
        }
    }

    private val productSelectionFlow = combine(
        filteredProductsFlow,
        selectedProductFlow,
        productUnitsFlow,
        categoryStateFlow
    ) { products: List<ProductEntity>, selectedProduct: ProductEntity?, productUnits: List<ProductUnitEntity>, categoryState: CategoryState ->
        ProductSelectionState(
            products = products,
            selectedProduct = selectedProduct,
            productUnits = productUnits,
            categories = categoryState.categories,
            selectedCategory = categoryState.selectedCategory
        )
    }

    private val searchMealDateFlow = combine(
        searchQuery,
        mealNameText,
        selectedDate
    ) { search: String, mealName: String, date: LocalDate ->
        SearchMealDateState(
            searchQuery = search,
            mealNameText = mealName,
            selectedDate = date
        )
    }

    private val amountUnitErrorFlow = combine(
        amountText,
        selectedUnitName,
        errorMessage
    ) { amount: String, unitName: String, error: String? ->
        AmountUnitErrorState(
            amountText = amount,
            selectedUnitName = unitName,
            errorMessage = error
        )
    }

    private val inputFlow = combine(
        searchMealDateFlow,
        amountUnitErrorFlow
    ) { searchMealDate: SearchMealDateState, amountUnitError: AmountUnitErrorState ->
        InputState(
            searchQuery = searchMealDate.searchQuery,
            mealNameText = searchMealDate.mealNameText,
            selectedDate = searchMealDate.selectedDate,
            amountText = amountUnitError.amountText,
            selectedUnitName = amountUnitError.selectedUnitName,
            errorMessage = amountUnitError.errorMessage
        )
    }

    val uiState = combine(
        productSelectionFlow,
        inputFlow,
        ingredients,
        editMealID,
        templateStateFlow
    ) { productSelection: ProductSelectionState, input: InputState, ingredientList: List<DraftMealIngredient>, currentEditMealID: Long?, templateState: TemplateState ->
        val calculatedValues = calculateMacros(
            product = productSelection.selectedProduct,
            productUnits = productSelection.productUnits,
            amountText = input.amountText,
            selectedUnitName = input.selectedUnitName

        )

        AddMealUiState(
            products = productSelection.products,
            categories = productSelection.categories,
            selectedCategory = productSelection.selectedCategory,
            selectedProduct = productSelection.selectedProduct,
            productUnits = productSelection.productUnits,
            templates = templateState.templates,
            showTemplates = templateState.showTemplates,
            templateSavedMessage = templateState.templateSavedMessage,
            editMealID = currentEditMealID,
            isEditMode = currentEditMealID != null,
            searchQuery = input.searchQuery,
            mealNameText = input.mealNameText,
            selectedDate = input.selectedDate.format(isoFormatter),
            selectedDateLabel = input.selectedDate.format(labelFormatter),
            canMoveToNextDay = input.selectedDate.isBefore(TrackingDateUtils.getCurrentTrackingDate()),
            amountText = input.amountText,
            selectedUnitName = input.selectedUnitName,
            calculatedKcal = calculatedValues.kcal,
            calculatedProtein = calculatedValues.protein,
            calculatedCarbs = calculatedValues.carbs,
            calculatedFat = calculatedValues.fat,
            ingredients = ingredientList,
            totalKcal = ingredientList.sumOf { it.kcal },
            totalProtein = ingredientList.sumOf { it.protein },
            totalCarbs = ingredientList.sumOf { it.carbs },
            totalFat = ingredientList.sumOf { it.fat },
            errorMessage = input.errorMessage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddMealUiState()
    )

    fun selectCategory(category: String) {
        selectedCategory.value = category
        errorMessage.value = null
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProductFavorite(
                productID = product.productID,
                isFavorite = !product.isFavorite
            )
        }
    }
    fun setEditMealID(mealID: Long?) {
        if (mealID == null) {
            editMealID.value = null
            return
        }

        if (loadedEditMealID == mealID) {
            return
        }

        loadedEditMealID = mealID
        editMealID.value = mealID
        errorMessage.value = null

        viewModelScope.launch {
            val mealWithEntries = repository.getMealWithEntriesByID(mealID)

            if (mealWithEntries == null) {
                errorMessage.value = "Nie znaleziono posiłku do edycji."
                return@launch
            }

            mealNameText.value = mealWithEntries.meal.name.orEmpty()
            selectedDate.value = LocalDate.parse(mealWithEntries.meal.date, isoFormatter)

            ingredients.value = mealWithEntries.entries.map { entry ->
                DraftMealIngredient(
                    draftIngredientID = nextDraftIngredientID++,
                    productID = entry.productID,
                    productName = entry.productName,
                    amount = entry.amount,
                    unitName = entry.unitName,
                    amountInBaseUnit = entry.amountInBaseUnit,
                    kcal = entry.kcal,
                    protein = entry.protein,
                    carbs = entry.carbs,
                    fat = entry.fat
                )
            }
        }
    }

    fun changeSearchQuery(value: String) {
        searchQuery.value = value
        errorMessage.value = null
    }
    fun changeMealName(value: String) {
        mealNameText.value = value
        errorMessage.value = null
    }

    fun toggleTemplates() {
        showTemplates.value = !showTemplates.value
        errorMessage.value = null
    }

    fun addTemplateToCurrentMeal(templateID: Long) {
        viewModelScope.launch {
            val template = repository.getMealTemplateWithEntriesByID(templateID)

            if (template == null) {
                errorMessage.value = "Nie znaleziono szablonu."
                return@launch
            }

            val draftIngredients = template.entries.map { entry ->
                DraftMealIngredient(
                    draftIngredientID = nextDraftIngredientID++,
                    productID = entry.productID,
                    productName = entry.productName,
                    amount = entry.amount,
                    unitName = entry.unitName,
                    amountInBaseUnit = entry.amountInBaseUnit,
                    kcal = entry.kcal,
                    protein = entry.protein,
                    carbs = entry.carbs,
                    fat = entry.fat
                )
            }

            ingredients.value = ingredients.value + draftIngredients

            if (mealNameText.value.isBlank()) {
                mealNameText.value = template.template.name
            }

            showTemplates.value = false
            errorMessage.value = null
        }
    }

    fun saveCurrentMealAsTemplate() {
        val state = uiState.value

        val templateName = state.mealNameText.trim()

        if (templateName.isBlank()) {
            errorMessage.value = "Wpisz nazwę posiłku, żeby zapisać go jako szablon."
            return
        }

        if (state.ingredients.isEmpty()) {
            errorMessage.value = "Dodaj składniki, zanim zapiszesz szablon."
            return
        }

        viewModelScope.launch {
            repository.addMealTemplate(
                name = templateName,
                ingredients = state.ingredients.map { it.toRepositoryDraft() }
            )

            errorMessage.value = null
            templateSavedMessage.value = "Dodano szablon."
        }
    }

    fun deleteTemplate(templateID: Long) {
        viewModelScope.launch {
            repository.deleteMealTemplateByID(templateID)
            errorMessage.value = null
        }
    }
    fun dismissTemplateSavedPopup() {
        templateSavedMessage.value = null
    }
    fun moveSelectedDateBack() {
        selectedDate.value = selectedDate.value.minusDays(1)
        errorMessage.value = null
    }

    fun moveSelectedDateForward() {
        val currentTrackingDate = TrackingDateUtils.getCurrentTrackingDate()

        if (selectedDate.value.isBefore(currentTrackingDate)) {
            selectedDate.value = selectedDate.value.plusDays(1)
        }

        errorMessage.value = null
    }

    fun selectCurrentTrackingDate() {
        selectedDate.value = TrackingDateUtils.getCurrentTrackingDate()
        errorMessage.value = null
    }

    fun selectProduct(product: ProductEntity) {
        selectedProductID.value = product.productID
        selectedUnitName.value = product.baseUnit
        errorMessage.value = null
    }

    fun changeAmount(value: String) {
        amountText.value = value.replace(",", ".")
        errorMessage.value = null
    }

    fun selectUnit(unitName: String) {
        selectedUnitName.value = unitName
        errorMessage.value = null
    }

    fun addSelectedIngredient() {
        val state = uiState.value
        val product = state.selectedProduct

        if (product == null) {
            errorMessage.value = "Wybierz produkt."
            return
        }

        val amount = state.amountText.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            errorMessage.value = "Wpisz poprawną ilość."
            return
        }

        if (state.selectedUnitName.isBlank()) {
            errorMessage.value = "Wybierz jednostkę."
            return
        }

        val amountInBaseUnit = calculateAmountInBaseUnit(
            product = product,
            productUnits = state.productUnits,
            amount = amount,
            selectedUnitName = state.selectedUnitName
        )

        val multiplier = amountInBaseUnit / 100.0

        val ingredient = DraftMealIngredient(
            draftIngredientID = nextDraftIngredientID++,
            productID = product.productID,
            productName = product.name,
            amount = amount,
            unitName = state.selectedUnitName,
            amountInBaseUnit = amountInBaseUnit,
            kcal = product.kcalPer100 * multiplier,
            protein = product.proteinPer100 * multiplier,
            carbs = product.carbsPer100 * multiplier,
            fat = product.fatPer100 * multiplier
        )

        ingredients.value = ingredients.value + ingredient

        amountText.value = ""
        errorMessage.value = null
    }

    fun removeIngredient(draftIngredientID: Long) {
        ingredients.value = ingredients.value.filterNot {
            it.draftIngredientID == draftIngredientID
        }

        errorMessage.value = null
    }

    fun saveMeal(onSuccess: () -> Unit) {
        val state = uiState.value

        if (state.ingredients.isEmpty()) {
            errorMessage.value = "Dodaj przynajmniej jeden składnik posiłku."
            return
        }

        viewModelScope.launch {
            val currentEditMealID = state.editMealID

            if (currentEditMealID == null) {
                repository.addMeal(
                    date = state.selectedDate,
                    name = state.mealNameText,
                    ingredients = state.ingredients.map { it.toRepositoryDraft() }
                )
            } else {
                repository.updateMeal(
                    mealID = currentEditMealID,
                    date = state.selectedDate,
                    name = state.mealNameText,
                    ingredients = state.ingredients.map { it.toRepositoryDraft() }
                )
            }

            app.refreshWidgets()
            onSuccess()
        }
    }

    private fun calculateMacros(
        product: ProductEntity?,
        productUnits: List<ProductUnitEntity>,
        amountText: String,
        selectedUnitName: String
    ): CalculatedMacros {
        if (product == null) {
            return CalculatedMacros()
        }

        val amount = amountText.toDoubleOrNull() ?: return CalculatedMacros()

        if (amount <= 0.0) {
            return CalculatedMacros()
        }

        val amountInBaseUnit = calculateAmountInBaseUnit(
            product = product,
            productUnits = productUnits,
            amount = amount,
            selectedUnitName = selectedUnitName
        )

        val multiplier = amountInBaseUnit / 100.0

        return CalculatedMacros(
            kcal = product.kcalPer100 * multiplier,
            protein = product.proteinPer100 * multiplier,
            carbs = product.carbsPer100 * multiplier,
            fat = product.fatPer100 * multiplier
        )
    }

    private fun calculateAmountInBaseUnit(
        product: ProductEntity,
        productUnits: List<ProductUnitEntity>,
        amount: Double,
        selectedUnitName: String
    ): Double {
        if (selectedUnitName == product.baseUnit) {
            return amount
        }

        val selectedUnit = productUnits.firstOrNull {
            it.unitName == selectedUnitName
        }

        return if (selectedUnit != null) {
            amount * selectedUnit.amountInBaseUnit
        } else {
            amount
        }
    }

    private data class ProductSelectionState(
        val products: List<ProductEntity> = emptyList(),
        val selectedProduct: ProductEntity? = null,
        val productUnits: List<ProductUnitEntity> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "Wszystkie"
    )

    private data class SearchMealDateState(
        val searchQuery: String = "",
        val mealNameText: String = "",
        val selectedDate: LocalDate = TrackingDateUtils.getCurrentTrackingDate()
    )

    private data class AmountUnitErrorState(
        val amountText: String = "",
        val selectedUnitName: String = "",
        val errorMessage: String? = null
    )

    private data class InputState(
        val searchQuery: String = "",
        val mealNameText: String = "",
        val selectedDate: LocalDate = TrackingDateUtils.getCurrentTrackingDate(),
        val amountText: String = "",
        val selectedUnitName: String = "",
        val errorMessage: String? = null
    )

    private data class CalculatedMacros(
        val kcal: Double = 0.0,
        val protein: Double = 0.0,
        val carbs: Double = 0.0,
        val fat: Double = 0.0
    )

    private data class TemplateState(
        val templates: List<MealTemplateWithEntries> = emptyList(),
        val showTemplates: Boolean = false,
        val templateSavedMessage: String? = null
    )
    private data class CategoryState(
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "Wszystkie"
    )
}