package com.example.myservice.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.InvoiceBySrAndDateRangeReq
import com.example.myservice.data.model.InvoiceCollectionResponse
import com.example.myservice.data.model.SR
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.launch

class InvoiceCollection(
    private val repository: InvoiceRepository
) : ViewModel() {
    // New filter states
    private val _srs = mutableStateListOf<SR>()
    val srs: List<SR> get() = _srs

    private val _invoices = mutableStateListOf<InvoiceCollectionResponse>()
    val invoices: List<InvoiceCollectionResponse> get() = _invoices

    var selectedSR by mutableStateOf<SR?>(null)

    var selectedInvoiceForCollection by mutableStateOf<InvoiceCollectionResponse?>(null)

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    //var selectedStartDate by mutableStateOf<String?>(null)

    var selectedEndDate by mutableStateOf<String?>(null)

    //var selectedSR by mutableStateOf<String?>(null)
    var selectedStartDate by mutableStateOf<String?>(null)

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
                            srId = 15, //selectedSR!!.id ?: throw Exception("SR not selected"),
                            startDate = selectedStartDate.toString(),
                            endDate = selectedEndDate.toString(),
                        )
                    )
                    if (response.isSuccessful) {
                        _invoices.clear()
                        response.body()?.let {
                            _invoices.addAll(it.data ?: emptyList())
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InvoiceCollection", "Error loading invoices", e)
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    fun updateSelectedInvoiceForCollection(invoice: InvoiceCollectionResponse?) {
        selectedInvoiceForCollection = invoice
    }

    fun collectInvoice(invoiceId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val success = true;//repository.collectInvoice(invoiceId)
                if (success) {
                    loadInvoices() // Refresh the list after collection
                } else {
                    //_uiState.update { it.copy(error = "Failed to collect invoice") }
                }
            } catch (e: Exception) {
                //_uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

