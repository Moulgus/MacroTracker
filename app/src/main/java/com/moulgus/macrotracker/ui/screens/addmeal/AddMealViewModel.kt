package com.moulgus.macrotracker.ui.screens.addmeal

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
import com.moulgus.macrotracker.util.TrackingDateUtils

@OptIn(ExperimentalCoroutinesApi::class)
class AddMealViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        (application as MacroTrackerApplication).repository

    private val selectedProductID = MutableStateFlow<Long?>(null)
    private val amountText = MutableStateFlow("")
    private val selectedUnitName = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)

    private val productsFlow = repository.observeAllProducts()

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
        productsFlow,
        selectedProductFlow,
        productUnitsFlow
    ) { products: List<ProductEntity>, selectedProduct: ProductEntity?, productUnits: List<ProductUnitEntity> ->
        ProductSelectionState(
            products = products,
            selectedProduct = selectedProduct,
            productUnits = productUnits
        )
    }

    private val inputFlow = combine(
        amountText,
        selectedUnitName,
        errorMessage
    ) { amount: String, unitName: String, error: String? ->
        InputState(
            amountText = amount,
            selectedUnitName = unitName,
            errorMessage = error
        )
    }

    val uiState = combine(
        productSelectionFlow,
        inputFlow
    ) { productSelection: ProductSelectionState, input: InputState ->
        val calculatedValues = calculateMacros(
            product = productSelection.selectedProduct,
            productUnits = productSelection.productUnits,
            amountText = input.amountText,
            selectedUnitName = input.selectedUnitName
        )

        AddMealUiState(
            products = productSelection.products,
            selectedProduct = productSelection.selectedProduct,
            productUnits = productSelection.productUnits,
            amountText = input.amountText,
            selectedUnitName = input.selectedUnitName,
            calculatedKcal = calculatedValues.kcal,
            calculatedProtein = calculatedValues.protein,
            calculatedCarbs = calculatedValues.carbs,
            calculatedFat = calculatedValues.fat,
            errorMessage = input.errorMessage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddMealUiState()
    )

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

    fun addMealEntry(onSuccess: () -> Unit) {
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

        viewModelScope.launch {
            repository.addMealEntry(
                product = product,
                date = getTodayDate(),
                amount = amount,
                unitName = state.selectedUnitName,
                amountInBaseUnit = amountInBaseUnit
            )

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

    private fun getTodayDate(): String {
        return TrackingDateUtils.getCurrentTrackingDateString()
    }

    private data class ProductSelectionState(
        val products: List<ProductEntity> = emptyList(),
        val selectedProduct: ProductEntity? = null,
        val productUnits: List<ProductUnitEntity> = emptyList()
    )

    private data class InputState(
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
}