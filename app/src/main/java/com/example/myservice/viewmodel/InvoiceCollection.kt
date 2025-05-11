package com.example.myservice.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.InvoiceBySrAndDateRangeReq
import com.example.myservice.data.model.InvoiceCollectionReq
import com.example.myservice.data.model.InvoiceCollectionResponse
import com.example.myservice.data.model.SR
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoiceCollection(
    private val repository: InvoiceRepository
) : ViewModel() {
    // New filter states
    private val _srs = mutableStateListOf<SR>()
    val srs: List<SR> get() = _srs

    private val _invoices = mutableStateListOf<InvoiceCollectionResponse>()
    val invoices: List<InvoiceCollectionResponse> get() = _invoices

    // Filter states
    var selectedSR by mutableStateOf<SR?>(null)

    var selectedInvoiceForCollection by mutableStateOf<InvoiceCollectionResponse?>(null)

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    //var selectedStartDate by mutableStateOf<String?>(null)

    var selectedEndDate by mutableStateOf<String?>(null)

    //var selectedSR by mutableStateOf<String?>(null)
    var selectedStartDate by mutableStateOf<String?>(null)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Add these new state variables
    var partySearchQuery by mutableStateOf("")
    var isPartyDropdownExpanded by mutableStateOf(false)
    private val _availableParties = mutableStateListOf<String>()
    val availableParties: List<String> get() = _availableParties

    val filteredInvoices: List<InvoiceCollectionResponse>
        get() = _invoices.filter {
            partySearchQuery.isEmpty() ||
                    it.businessName.contains(partySearchQuery, ignoreCase = true)
        }

    // Update when invoices are loaded
    private fun updateAvailableParties() {
        _availableParties.clear()
        _availableParties.addAll(
            _invoices.map { it.businessName }.distinct().sorted()
        )
    }

    init {
        loadSRs()
    }

    private fun loadSRs() {
        Log.d("SHAKIL", "LOAD srs IS GETTING CALLED")
        viewModelScope.launch {

            try {
                val response = repository.getSRs()
                if (response.isSuccessful) {
                    _srs.clear()
                    response.body()?.let {
                        _srs.addAll(it.data ?: emptyList())
                    }
                }
                Log.d("SHAKIL", srs.toString());
                //_uiState.update { it.copy(srs = srs, srsLoading = false) }
            } catch (e: Exception) {
                //_uiState.update { it.copy(srsError = e.message, srsLoading = false) }
            }
        }
    }

    // --- Add functions to update the filter states ---
    fun updateSelectedSR(sr: SR?) {
        selectedSR = sr
    }

    fun updateSelectedStartDate(date: String?) {
        // Optional: Add validation if needed
        selectedStartDate = date
    }

    fun updateSelectedEndDate(date: String?) {
        // Optional: Add validation if needed
        selectedEndDate = date
        loadInvoices()
    }

    // Filter management
    fun clearFilters() {
        selectedSR = null
        selectedStartDate = null
        selectedEndDate = null
    }







    fun loadInvoices() {
        Log.d("SHAKIL", "LOAD INVOICE collection IS GETTING CALLED");
        //val state = _uiState.value

        if (selectedSR != null && selectedStartDate != null && selectedEndDate != null) {
            viewModelScope.launch {

                try {
                    val response = repository.getInvoiceBySrAndDateRange(
                        InvoiceBySrAndDateRangeReq(
                            srId = selectedSR!!.id ?: throw Exception("SR not selected"),
                            startDate = selectedStartDate.toString(),
                            endDate = selectedEndDate.toString(),
                        )
                    )
                    if (response.isSuccessful) {
                        _invoices.clear()
                        response.body()?.let {
                            _invoices.addAll(it.data ?: emptyList())
                        }
                        updateAvailableParties()
                        clearSearch()
                    }
                } catch (e: Exception) {
                    _toastMessage.value = "Error loading invoices: ${e.message}"
                    Log.e("InvoiceCollection", "Error loading invoices", e)
                } finally {
                    _loading.value = false
                }
            }
        } else {
            _toastMessage.value = "Please select SR and date range"
            return
        }
    }
    // Clear search functionality
    fun clearSearch() {
        partySearchQuery = ""
        isPartyDropdownExpanded = false
    }

    fun updateSelectedInvoiceForCollection(invoice: InvoiceCollectionResponse?) {
        selectedInvoiceForCollection = invoice
    }

    fun collectInvoice(invoiceId: Int, amount: Double, collectionDateBySR: String) {
        viewModelScope.launch {
            try {
                val response = repository.collectInvoiceBySR(
                    InvoiceCollectionReq(
                        partyId = selectedInvoiceForCollection?.id ?: 4,
                        collectionAmount = amount.toInt(),
                        transactionDate = selectedInvoiceForCollection?.invoiceDate ?: "2025-04-14",
                        collectionDate = collectionDateBySR.toString()
                    )
                )
                Log.d("SHAKIL", response.toString())
                if (response.isSuccessful) {
                    response.body()?.let { collectionResult ->
                        _toastMessage.value = collectionResult.message
                    } ?: run {
                        _toastMessage.value = "Invoice collected successfully"
                    }
                    Log.d("SHAKIL", response.toString())

                    loadInvoices() // Refresh the list after collection
                } else {
                    _toastMessage.value = "Failed to collect invoice: ${response.message()}"
                    //_uiState.update { it.copy(error = "Failed to collect invoice") }
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                //_uiState.update { it.copy(error = e.message) }
            }
        }

    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

}

