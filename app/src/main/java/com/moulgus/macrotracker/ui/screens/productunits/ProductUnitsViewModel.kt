package com.moulgus.macrotracker.ui.screens.productunits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moulgus.macrotracker.MacroTrackerApplication
import com.moulgus.macrotracker.data.local.entity.ProductEntity
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductUnitsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        (application as MacroTrackerApplication).repository

    private val selectedProductID = MutableStateFlow<Long?>(null)
    private val productState = MutableStateFlow<ProductEntity?>(null)

    private val unitNameText = MutableStateFlow("")
    private val amountInBaseUnitText = MutableStateFlow("")

    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    private val unitsFlow = selectedProductID.flatMapLatest { productID ->
        if (productID == null) {
            flowOf(emptyList())
        } else {
            repository.observeUnitsForProduct(productID)
        }
    }

    private val formFlow = combine(
        unitNameText,
        amountInBaseUnitText
    ) { unitName: String, amount: String ->
        FormState(
            unitNameText = unitName,
            amountInBaseUnitText = amount
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

    val uiState = combine(
        productState,
        unitsFlow,
        formFlow,
        messageFlow
    ) { product: ProductEntity?, units: List<ProductUnitEntity>, form: FormState, messages: MessageState ->
        ProductUnitsUiState(
            product = product,
            units = units,
            unitNameText = form.unitNameText,
            amountInBaseUnitText = form.amountInBaseUnitText,
            errorMessage = messages.errorMessage,
            successMessage = messages.successMessage,
            isLoading = product == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductUnitsUiState()
    )

    fun setProductID(productID: Long) {
        if (selectedProductID.value == productID) {
            return
        }

        selectedProductID.value = productID
        productState.value = null
        clearMessages()

        viewModelScope.launch {
            productState.value = repository.getProductByID(productID)
        }
    }

    fun changeUnitName(value: String) {
        unitNameText.value = value
        clearMessages()
    }

    fun changeAmountInBaseUnit(value: String) {
        amountInBaseUnitText.value = value.replace(",", ".")
        clearMessages()
    }

    fun addUnit() {
        val product = uiState.value.product
        val productID = selectedProductID.value

        if (product == null || productID == null) {
            errorMessage.value = "Nie wybrano produktu."
            return
        }

        val unitName = unitNameText.value.trim()
        val amountInBaseUnit = amountInBaseUnitText.value.toDoubleOrNull()

        if (unitName.isBlank()) {
            errorMessage.value = "Wpisz nazwę jednostki."
            return
        }

        if (unitName.equals(product.baseUnit, ignoreCase = true)) {
            errorMessage.value = "Jednostka bazowa jest już dostępna."
            return
        }

        if (uiState.value.units.any { it.unitName.equals(unitName, ignoreCase = true) }) {
            errorMessage.value = "Taka jednostka już istnieje dla tego produktu."
            return
        }

        if (amountInBaseUnit == null || amountInBaseUnit <= 0.0) {
            errorMessage.value = "Wpisz poprawną wartość przelicznika."
            return
        }

        viewModelScope.launch {
            repository.addProductUnit(
                productID = productID,
                unitName = unitName,
                amountInBaseUnit = amountInBaseUnit
            )

            unitNameText.value = ""
            amountInBaseUnitText.value = ""

            errorMessage.value = null
            successMessage.value = "Dodano jednostkę."
        }
    }

    fun deleteUnit(unit: ProductUnitEntity) {
        viewModelScope.launch {
            repository.deleteProductUnit(unit)
            errorMessage.value = null
            successMessage.value = "Usunięto jednostkę."
        }
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }

    private data class FormState(
        val unitNameText: String = "",
        val amountInBaseUnitText: String = ""
    )

    private data class MessageState(
        val errorMessage: String? = null,
        val successMessage: String? = null
    )
}