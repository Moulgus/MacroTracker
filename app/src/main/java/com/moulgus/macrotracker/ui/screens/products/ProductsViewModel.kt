package com.moulgus.macrotracker.ui.screens.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        (application as MacroTrackerApplication).repository

    private val categoryAll = "Wszystkie"
    private val categoryFavorites = "Ulubione"

    private val formState = MutableStateFlow(ProductFormState())
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(categoryAll)

    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    private val productsFlow = repository.observeAllProducts()

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

    private val messageFlow = combine(
        errorMessage,
        successMessage
    ) { error: String?, success: String? ->
        MessageState(
            errorMessage = error,
            successMessage = success
        )
    }

    private val productListStateFlow = combine(
        filteredProductsFlow,
        categoryStateFlow,
        searchQuery
    ) { products: List<ProductEntity>, categoryState: CategoryState, search: String ->
        ProductListState(
            products = products,
            searchQuery = search,
            categories = categoryState.categories,
            selectedCategory = categoryState.selectedCategory
        )
    }

    private val formMessageStateFlow = combine(
        formState,
        messageFlow
    ) { form: ProductFormState, messages: MessageState ->
        FormMessageState(
            form = form,
            errorMessage = messages.errorMessage,
            successMessage = messages.successMessage
        )
    }

    val uiState = combine(
        productListStateFlow,
        formMessageStateFlow
    ) { productList: ProductListState, formMessage: FormMessageState ->
        ProductsUiState(
            products = productList.products,
            searchQuery = productList.searchQuery,
            categories = productList.categories,
            selectedCategory = productList.selectedCategory,
            form = formMessage.form,
            errorMessage = formMessage.errorMessage,
            successMessage = formMessage.successMessage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductsUiState()
    )

    fun changeSearchQuery(value: String) {
        searchQuery.value = value
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProductFavorite(
                productID = product.productID,
                isFavorite = !product.isFavorite
            )
        }
    }

    fun changeName(value: String) {
        formState.value = formState.value.copy(nameText = value)
        clearMessages()
    }

    fun changeCategory(value: String) {
        formState.value = formState.value.copy(categoryText = value)
        clearMessages()
    }

    fun changeBaseUnit(value: String) {
        formState.value = formState.value.copy(baseUnit = value)
        clearMessages()
    }

    fun changeKcal(value: String) {
        formState.value = formState.value.copy(kcalText = normalizeDecimalInput(value))
        clearMessages()
    }

    fun changeProtein(value: String) {
        formState.value = formState.value.copy(proteinText = normalizeDecimalInput(value))
        clearMessages()
    }

    fun changeCarbs(value: String) {
        formState.value = formState.value.copy(carbsText = normalizeDecimalInput(value))
        clearMessages()
    }

    fun changeFat(value: String) {
        formState.value = formState.value.copy(fatText = normalizeDecimalInput(value))
        clearMessages()
    }

    fun addProduct(onSuccess: (() -> Unit)? = null) {
        val form = formState.value

        val name = form.nameText.trim()
        val category = form.categoryText.trim().ifBlank { "Inne" }

        if (name.isBlank()) {
            errorMessage.value = "Wpisz nazwę produktu."
            return
        }

        val kcal = form.kcalText.toDoubleOrNull()
        val protein = form.proteinText.toDoubleOrNull()
        val carbs = form.carbsText.toDoubleOrNull()
        val fat = form.fatText.toDoubleOrNull()

        if (kcal == null || kcal < 0.0) {
            errorMessage.value = "Wpisz poprawne kcal na 100 ${form.baseUnit}."
            return
        }

        if (protein == null || protein < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość białka."
            return
        }

        if (carbs == null || carbs < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość węgli."
            return
        }

        if (fat == null || fat < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość tłuszczu."
            return
        }

        viewModelScope.launch {
            repository.addProduct(
                name = name,
                category = category,
                baseUnit = form.baseUnit,
                kcalPer100 = kcal,
                proteinPer100 = protein,
                carbsPer100 = carbs,
                fatPer100 = fat,
                isCustom = true
            )

            formState.value = ProductFormState()
            errorMessage.value = null
            successMessage.value = "Dodano produkt."

            onSuccess?.invoke()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        if (!product.isCustom) {
            errorMessage.value = "Nie usuwamy produktów domyślnych na tym etapie."
            return
        }

        viewModelScope.launch {
            repository.deleteProduct(product)
            successMessage.value = "Usunięto produkt."
            errorMessage.value = null
        }
    }

    private fun normalizeDecimalInput(value: String): String {
        return value.replace(",", ".")
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }

    private data class CategoryState(
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "Wszystkie"
    )

    private data class MessageState(
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    private data class ProductListState(
        val products: List<ProductEntity> = emptyList(),
        val searchQuery: String = "",
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "Wszystkie"
    )

    private data class FormMessageState(
        val form: ProductFormState = ProductFormState(),
        val errorMessage: String? = null,
        val successMessage: String? = null
    )
}