package com.example.myservice.viewmodel

import android.util.Log
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.launch

// AdminViewModel.kt
class AdminViewModel (
    private val invoiceRepository: InvoiceRepository
): ViewModel() {
    private val _invoices = mutableStateListOf<Invoice>()
    val invoices: List<Invoice> get() = _invoices

    private val _selectedInvoice = mutableStateOf<Invoice?>(null)
    val selectedInvoice: Invoice? get() = _selectedInvoice.value

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    fun selectInvoice(invoice: Invoice) {
        _selectedInvoice.value = invoice
    }

    //private val apiService = RetrofitClient.instance.create(InvoiceApi::class.java)

    init {
        loadAllInvoices()
    }

    /*fun refreshInvoiceList() {
        loadAllInvoices()
    }*/
    //refresh invoice list
    fun checkForNewInvoices() {
        Log.d("SHAKIL", "yep this luanched is getting called");
        viewModelScope.launch {
            try {
                val response = invoiceRepository.getAllInvoices()
                if (response.isSuccessful) {
                    response.body()?.let { invoiceResponse ->
                        if (invoiceResponse.success) {
                            val newList = invoiceResponse.data.orEmpty()
                            if (newList.isNotEmpty()) {
                                val currentIds = _invoices.map { it.id }.toSet()
                                val newInvoices = newList.filter { it.id !in currentIds }

                                // Add new invoices to the top
                                if (newInvoices.isNotEmpty()) {
                                    Log.d("SHAKIL", "yep new invoices are found");
                                    _invoices.addAll(0, newInvoices)
                                } else {
                                    Log.d("SHAKIL", "..no new invoices are found");
                                }
                            } else {
                                Log.d("SHAKIL", ".....no new invoices are found may be newList is empty");
                            }
                        } else {
                            Log.d("AdminViewModel", "Check failed: ${invoiceResponse.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error checking new invoices", e)
            }
        }
    }



    private fun loadAllInvoices() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = invoiceRepository.getAllInvoices()
                if (response.isSuccessful) {
                    response.body()?.let { invoiceResponse ->
                        if (invoiceResponse.success) {
                            _invoices.clear()
                            invoiceResponse.data?.let { data ->
                                _invoices.addAll(data)
                            }
                        } else {
                            Log.d("AdminViewModel", "Response unsuccessful: ${invoiceResponse.message}")
                        }
                    } ?: run {
                        Log.d("AdminViewModel", "Empty server response")
                    }
                }
            } catch (e: Exception) {
                // Handle error
                Log.d("SHAKIL", e.stackTraceToString());
            } finally {
                _loading.value = false
            }
        }
    }
/*
    fun loadInvoiceDetails(invoiceId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = invoiceRepository.getInvoice(invoiceId)
                if (response.isSuccessful) {
                    _selectedInvoice.value = response.body()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateInvoice(updatedInvoice: Invoice) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = apiService.updateInvoice(updatedInvoice.id, updatedInvoice)
                if (response.isSuccessful) {
                    val index = _invoices.indexOfFirst { it.id == updatedInvoice.id }
                    if (index != -1) {
                        _invoices[index] = updatedInvoice
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }
    */
}