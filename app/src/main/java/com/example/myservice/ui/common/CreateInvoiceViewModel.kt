package com.example.myservice.ui.common

// CreateInvoiceViewModel.kt
import CreateInvoiceRequest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateInvoiceViewModel(
    private val repository: InvoiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateInvoiceState())
    val uiState: StateFlow<CreateInvoiceState> = _uiState

    init {
        loadParties()
        loadProducts()
    }

    private fun loadParties() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingParties = true, partiesError = null) }
            try {
                val response = repository.getParties()
                val parties = response.body()?.parties ?: emptyList()
                _uiState.update {
                    it.copy(
                        parties = parties.map { p -> DropdownItem(p.id, p.businessName) },
                        filteredParties = parties.map { p -> DropdownItem(p.id, p.businessName) },
                        loadingParties = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingParties = false,
                        partiesError = "Failed to load parties: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProducts = true, productsError = null) }
            try {
                val response = repository.getProducts()
                val products = response.body()?.products ?: emptyList()
                _uiState.update {
                    it.copy(
                        products = products.map { p -> DropdownItem(p.id, p.name) },
                        loadingProducts = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingProducts = false,
                        productsError = "Failed to load products: ${e.message}"
                    )
                }
            }
        }
    }

    fun createInvoice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val response = repository.createInvoice(
                    CreateInvoiceRequest(
                        partyId = _uiState.value.selectedParty?.id ?: throw Exception("Party not selected"),
                        productId = _uiState.value.selectedProduct?.id ?: throw Exception("Product not selected"),
                        unitPrice = _uiState.value.unitPrice.toDouble(),
                        quantity = _uiState.value.totalCount.toInt(),
                        collectedAmount = _uiState.value.collectedMoney.toDouble(),
                        date = (_uiState.value.date)
                    )
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to create invoice: ${e.message}"
                    )
                }
            }
        }
    }


    /*fun select(it: DropdownItem) {
        //_uiState.update { state -> state.copy(selectedParty = it) }
    }*/

    fun selectProduct(it: DropdownItem) {
        _uiState.update { state -> state.copy(selectedProduct = it) }
    }

    fun updateUnitPrice(value: String) {
        _uiState.update { state -> state.copy(unitPrice = value) }
    }

    fun updateTotalCount(value: String) {
        _uiState.update { state -> state.copy(totalCount = value) }
    }

    fun updateCollectedMoney(value: String) {
        _uiState.update { state -> state.copy(collectedMoney = value) }
    }

    fun updateDate(value: String) {
        _uiState.update { state -> state.copy(date = value) }
    }

    // for search field of party

    fun updatePartySearch(query: String) {
        _uiState.update { state ->
            state.copy(
                partySearchQuery = query,
                filteredParties = filterParties(state.parties, query))
        }
    }

    private fun filterParties(parties: List<DropdownItem>, query: String): List<DropdownItem> {
        return if (query.isEmpty()) {
            parties
        } else {
            parties.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun select(party: DropdownItem?) { // Make parameter nullable
        Log.d("SHAKIL", party.toString())
        _uiState.update { state ->
            state.copy(selectedParty = party)
        }
    }
}

data class CreateInvoiceState(
    val parties: List<DropdownItem> = emptyList(),
    val products: List<DropdownItem> = emptyList(),
    val selectedParty: DropdownItem? = null,
    val selectedProduct: DropdownItem? = null,
    val unitPrice: String = "",
    val totalCount: String = "",
    val collectedMoney: String = "0",
    val date: String = (LocalDate.now().format(DateTimeFormatter.ISO_DATE)),
    val loadingParties: Boolean = false,
    val loadingProducts: Boolean = false,
    val partiesError: String? = null,
    val productsError: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val partySearchQuery: String = "",
    val filteredParties: List<DropdownItem> = emptyList(),
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = selectedParty != null &&
                selectedProduct != null &&
                unitPrice.isNotBlank() &&
                totalCount.isNotBlank() &&
                collectedMoney.isNotBlank() &&
                date.isNotBlank()
}

