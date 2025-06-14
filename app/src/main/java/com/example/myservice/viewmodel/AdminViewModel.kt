package com.example.myservice.viewmodel

import android.util.Log
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
import com.example.myservice.ui.common.DropdownItem
import com.example.myservice.util.ConnectivityService
import com.example.myservice.util.user.NetworkUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import toDropdownItem
import toLocalDateOrNull
import toParty

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

    private val dropdownItems: List<DropdownItem> get() = parties.map { it.toDropdownItem() }

    val filteredDropdownItems by derivedStateOf {
        if (searchQuery.isBlank()) {
            dropdownItems
        } else {
            dropdownItems.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var selectedDropdownItem by mutableStateOf<DropdownItem?>(null)


    var searchQuery by mutableStateOf("")
    private var selectedPartyItem by mutableStateOf<Party?>(null)
    /*val filteredPartyItems by derivedStateOf {
        if (searchQuery.isBlank()) {
            parties
        } else {
            parties.filter {
                it.businessName.contains(searchQuery, ignoreCase = true)
            }
        }
    }*/

    var isPartyLoading by mutableStateOf(false)
    var partyError by mutableStateOf<String?>(null)


    val filteredInvoices = derivedStateOf {
        val query = searchQuery.trim()
        val startDate = selectedStartDate?.toLocalDateOrNull()
        val endDate = selectedEndDate?.toLocalDateOrNull()

        _invoices.filter { invoice ->
            val invoiceParty = parties.find { it.id == invoice.partyId }
            val nameMatch = if (query.isNotEmpty()) {
                invoiceParty?.businessName?.contains(query, ignoreCase = true) == true
            } else {
                true
            }

            val dateMatch = when {
                startDate != null && endDate != null ->
                    invoice.date.toLocalDateOrNull()!! in startDate..endDate

                startDate != null ->
                    invoice.date.toLocalDateOrNull()?.isAfter(startDate) ?: false

                endDate != null ->
                    invoice.date.toLocalDateOrNull()?.isBefore(endDate) ?: false

                else -> true
            }

            nameMatch && dateMatch
        }
    }

    // Event for new invoice counts
    private val _newInvoiceEventChannel = Channel<Int>(Channel.BUFFERED)
    val newInvoiceEventFlow = _newInvoiceEventChannel.receiveAsFlow()

    private val _offlineEventChannel = Channel<Unit>()
    val offlineEventFlow = _offlineEventChannel.receiveAsFlow()

    fun handleOffline() {
        viewModelScope.launch {
            _offlineEventChannel.send(Unit)
        }
    }

    private val _retryEventChannel = Channel<Unit>()
    val retryEventFlow = _retryEventChannel.receiveAsFlow()

    fun retryLoadInvoices() {
        Log.d("SHAKIL", "retry function is getting called")
        initLoad()
    }



    // --- Add functions to update the filter states ---
    fun updateSelectedDropdownItem(item: DropdownItem?) {
        selectedDropdownItem = item
        val party = item?.toParty(parties)
        selectedParty = party
        selectedPartyItem = party // ✅ Ensures filteredInvoices gets updated
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
        Log.d("SHAKIL", "init function is getting called")
        initLoad()
    }

    private fun initLoad() {
        if(filteredInvoices.value.isEmpty()) {
            viewModelScope.launch {
                loadAllInvoices()
            }
        } else {
            viewModelScope.launch {
                checkForNewInvoices()
            }
        }
        if (parties.isEmpty()) {
            viewModelScope.launch {
                loadParties()
            }
        }
    }

    fun refresh() {
        if (parties.isEmpty()) {
            viewModelScope.launch {
                loadParties()
            }
        }
        viewModelScope.launch {
            checkForNewInvoices()
        }
    }

    private suspend fun loadParties() {

            isPartyLoading = true
            partyError = null
            try {
                val response = invoiceRepository.getParties()
                if (response.isSuccessful) {
                    response.body()?.let { partyResponse ->
                        if (partyResponse.success) {
                            _parties.clear()
                            partyResponse.parties.let { party ->
                                _parties.addAll(party)
                            }
                        } else {
                            partyError =
                                "Something went wrong!!! party response success false." //partyResponse.message
                        }
                    }
                } else {
                    partyError = "Failed to load parties: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading parties", e)
                partyError = "Error loading parties: ${e.localizedMessage}"
            } finally {
                isPartyLoading = false
            }

    }

    //refresh invoice list
    private suspend fun checkForNewInvoices() {
        Log.d("SHAKIL", "checkForNewInvoices() called")

            try {
                val response = invoiceRepository.getAllInvoices()
                if (!response.isSuccessful) {
                    Log.e("AdminViewModel", "Server error: ${response.code()}")
                    //return@launch
                }

                val invoiceResponse = response.body()
                if (invoiceResponse == null || !invoiceResponse.success) {
                    Log.e("AdminViewModel", "API failure or empty body")
                    //return@launch
                }

                val fetched = invoiceResponse?.data.orEmpty()

                // 1) Build a map of fetched invoices by ID for quick lookup
                val fetchedById = fetched.associateBy { it.id }

                // 2) For each existing invoice, replace it if there's a newer version
                val replaced = _invoices.map { existing ->
                    fetchedById[existing.id] ?: existing
                }

                // 3) Collect truly new invoices (IDs not already in existing list)
                val existingIds = _invoices.map { it.id }.toSet()
                val newOnes = fetched.filter { it.id !in existingIds }

                if (newOnes.isNotEmpty()) {
                    _newInvoiceEventChannel.send(newOnes.size)
                }

                // 4) Combine: new ones at front, then replaced/unchanged
                _invoices.clear()
                _invoices.addAll(0, newOnes)
                _invoices.addAll(replaced)

                Log.d("SHAKIL", "Invoices updated: total now = ${_invoices.size}")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error checking new invoices", e)
            }

    }

    private suspend fun loadAllInvoices() {

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
                            Log.d(
                                "AdminViewModel",
                                "Response unsuccessful: ${invoiceResponse.message}"
                            )
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

