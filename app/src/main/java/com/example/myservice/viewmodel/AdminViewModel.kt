package com.example.myservice.viewmodel

import android.util.Log
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.Party
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

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

    // New filter states
    private val _parties = mutableStateListOf<Party>()
    val parties: List<Party> get() = _parties

    var selectedParty by mutableStateOf<Party?>(null)

    var selectedStartDate by mutableStateOf<String?>(null)

    var selectedEndDate by mutableStateOf<String?>(null)

    val filteredInvoices = derivedStateOf {
        val currentParty = selectedParty
        val startDate = selectedStartDate?.toLocalDateOrNull()
        val endDate = selectedEndDate?.toLocalDateOrNull()

        _invoices.filter { invoice ->
            val partyMatch = currentParty?.let { invoice.partyId.toString() == it.id.toString() } ?: true
            val dateMatch = when {
                startDate != null && endDate != null ->
                    invoice.date.toLocalDateOrNull()!! in startDate..endDate
                startDate != null ->
                    invoice.date.toLocalDateOrNull()?.isAfter(startDate) ?: false
                endDate != null ->
                    invoice.date.toLocalDateOrNull()?.isBefore(endDate) ?: false
                else -> true
            }
            partyMatch && dateMatch
        }
    }

    // --- Add functions to update the filter states ---
    fun updateSelectedParty(party: Party?) {
        selectedParty = party
    }

    fun updateSelectedStartDate(date: String?) {
        // Optional: Add validation if needed
        selectedStartDate = date
    }

    fun updateSelectedEndDate(date: String?) {
        // Optional: Add validation if needed
        selectedEndDate = date
    }

    // Filter management
    fun clearFilters() {
        selectedParty = null
        selectedStartDate = null
        selectedEndDate = null
    }

    fun selectInvoice(invoice: Invoice) {
        _selectedInvoice.value = invoice
    }

    //private val apiService = RetrofitClient.instance.create(InvoiceApi::class.java)

    init {
        loadAllInvoices()
        loadParties()
    }

    private fun loadParties() {
        viewModelScope.launch {
            try {
                val response = invoiceRepository.getParties()
                if (response.isSuccessful) {
                    response.body()?.let { partyResponse ->
                        if (partyResponse.success) {
                            _parties.clear()
                            partyResponse.parties.let { party ->
                                _parties.addAll(party)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading parties", e)
            }
        }
    }

    /*fun refreshInvoiceList() {
        loadAllInvoices()
    }*/
    //refresh invoice list
    fun checkForNewInvoices() {
        Log.d("SHAKIL", "checkForNewInvoices() called")
        viewModelScope.launch {
            try {
                val response = invoiceRepository.getAllInvoices()
                if (!response.isSuccessful) {
                    Log.e("AdminViewModel", "Server error: ${response.code()}")
                    return@launch
                }

                val invoiceResponse = response.body()
                if (invoiceResponse == null || !invoiceResponse.success) {
                    Log.e("AdminViewModel", "API failure or empty body")
                    return@launch
                }

                val fetched = invoiceResponse.data.orEmpty()

                // 1) Build a map of fetched invoices by ID for quick lookup
                val fetchedById = fetched.associateBy { it.id }

                // 2) For each existing invoice, replace it if there's a newer version
                val replaced = _invoices.map { existing ->
                    fetchedById[existing.id] ?: existing
                }

                // 3) Collect truly new invoices (IDs not already in existing list)
                val existingIds = _invoices.map { it.id }.toSet()
                val newOnes = fetched.filter { it.id !in existingIds }

                // 4) Combine: new ones at front, then replaced/unchanged
                _invoices.clear()
                _invoices.addAll(0, newOnes)
                _invoices.addAll(replaced)

                Log.d("SHAKIL", "Invoices updated: total now = ${_invoices.size}")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error checking new invoices", e)
            }
        }
    }

    fun checkForNewInvoices2() {
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

// Helper extensions for date handling
private fun String?.toLocalDateOrNull(): LocalDate? = try {
    this?.let { LocalDate.parse(it) }
} catch (e: DateTimeParseException) {
    null
}

private fun LocalDate?.isInRange(start: LocalDate, end: LocalDate): Boolean {
    return this != null && (this >= start && this <= end)
}

