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

    private val editProductID = MutableStateFlow<Long?>(null)
    private val editingProduct = MutableStateFlow<ProductEntity?>(null)

    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    private val productsFlow = repository.observeAllProducts()

    private var loadedEditProductID: Long? = null

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

    private val formEditStateFlow = combine(
        formState,
        editProductID,
        editingProduct
    ) { form: ProductFormState, currentEditProductID: Long?, currentEditingProduct: ProductEntity? ->
        FormEditState(
            form = form,
            editProductID = currentEditProductID,
            isEditMode = currentEditingProduct != null
        )
    }

    private val formMessageStateFlow = combine(
        formEditStateFlow,
        messageFlow
    ) { formEdit: FormEditState, messages: MessageState ->
        FormMessageState(
            form = formEdit.form,
            editProductID = formEdit.editProductID,
            isEditMode = formEdit.isEditMode,
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
            editProductID = formMessage.editProductID,
            isEditMode = formMessage.isEditMode,
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

    fun setEditProductID(productID: Long?) {
        if (productID == null) {
            editProductID.value = null
            editingProduct.value = null
            loadedEditProductID = null
            formState.value = ProductFormState()
            clearMessages()
            return
        }

        if (loadedEditProductID == productID) {
            return
        }

        loadedEditProductID = productID
        editProductID.value = productID
        editingProduct.value = null
        clearMessages()

        viewModelScope.launch {
            val product = repository.getProductByID(productID)

            if (product == null) {
                errorMessage.value = "Nie znaleziono produktu."
                return@launch
            }

            editingProduct.value = product

            formState.value = ProductFormState(
                nameText = product.name,
                categoryText = product.category,
                baseUnit = product.baseUnit,
                kcalText = product.kcalPer100.toInputText(),
                proteinText = product.proteinPer100.toInputText(),
                carbsText = product.carbsPer100.toInputText(),
                fatText = product.fatPer100.toInputText()
            )
        }
    }

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

    fun saveProduct(onSuccess: (() -> Unit)? = null) {
        val validatedForm = validateForm() ?: return
        val productToEdit = editingProduct.value

        viewModelScope.launch {
            if (productToEdit == null) {
                repository.addProduct(
                    name = validatedForm.name,
                    category = validatedForm.category,
                    baseUnit = validatedForm.baseUnit,
                    kcalPer100 = validatedForm.kcal,
                    proteinPer100 = validatedForm.protein,
                    carbsPer100 = validatedForm.carbs,
                    fatPer100 = validatedForm.fat,
                    isCustom = true
                )

                successMessage.value = "Dodano produkt."
            } else {
                val updatedProduct = productToEdit.copy(
                    name = validatedForm.name,
                    category = validatedForm.category,
                    baseUnit = validatedForm.baseUnit,
                    kcalPer100 = validatedForm.kcal,
                    proteinPer100 = validatedForm.protein,
                    carbsPer100 = validatedForm.carbs,
                    fatPer100 = validatedForm.fat
                )

                repository.updateProduct(updatedProduct)

                successMessage.value = "Zapisano zmiany."
            }

            formState.value = ProductFormState()
            errorMessage.value = null

            onSuccess?.invoke()
        }
    }

    fun addProduct(onSuccess: (() -> Unit)? = null) {
        saveProduct(onSuccess = onSuccess)
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

    private fun validateForm(): ValidatedProductForm? {
        val form = formState.value

        val name = form.nameText.trim()
        val category = form.categoryText.trim().ifBlank { "Inne" }
        val baseUnit = form.baseUnit

        if (name.isBlank()) {
            errorMessage.value = "Wpisz nazwę produktu."
            return null
        }

        val kcal = form.kcalText.toDoubleOrNull()
        val protein = form.proteinText.toDoubleOrNull()
        val carbs = form.carbsText.toDoubleOrNull()
        val fat = form.fatText.toDoubleOrNull()

        if (kcal == null || kcal < 0.0) {
            errorMessage.value = "Wpisz poprawne kcal na 100 $baseUnit."
            return null
        }

        if (protein == null || protein < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość białka."
            return null
        }

        if (carbs == null || carbs < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość węgli."
            return null
        }

        if (fat == null || fat < 0.0) {
            errorMessage.value = "Wpisz poprawną ilość tłuszczu."
            return null
        }

        return ValidatedProductForm(
            name = name,
            category = category,
            baseUnit = baseUnit,
            kcal = kcal,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
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

    private data class ValidatedProductForm(
        val name: String,
        val category: String,
        val baseUnit: String,
        val kcal: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double
    )

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

    private data class FormEditState(
        val form: ProductFormState = ProductFormState(),
        val editProductID: Long? = null,
        val isEditMode: Boolean = false
    )

    private data class FormMessageState(
        val form: ProductFormState = ProductFormState(),
        val editProductID: Long? = null,
        val isEditMode: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )
}