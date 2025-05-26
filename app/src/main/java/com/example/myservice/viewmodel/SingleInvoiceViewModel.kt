package com.example.myservice.viewmodel

import UpdateInvoiceRequest
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.Party
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.common.DropdownItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import toDropdownItem
import toParty

class SingleInvoiceViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _invoice = mutableStateOf<Invoice?>(null)
    val invoice get() = _invoice

    private val _loading = mutableStateOf(true)
    val loading get() = _loading

    private val _error = mutableStateOf<String?>(null)
    val error get() = _error

    private val _srName = MutableLiveData<String?>()
    val srName: LiveData<String?> = _srName

    // Add new state variables for edit invoice functionality
    var showEditSheet by mutableStateOf(false)
    var updateLoading by mutableStateOf(false)
    var updateError by mutableStateOf<String?>(null)

    // Add party-related state
    private val _parties = mutableStateListOf<Party>()
    val parties: List<Party> get() = _parties

    val dropdownItems: List<DropdownItem> get() = parties.map { it.toDropdownItem() }

    var searchQuery by mutableStateOf("")
    private var selectedPartyItem by mutableStateOf<Party?>(null)
    // Add these state variables
    private val _filteredDropdownItems = mutableStateListOf<DropdownItem>()
    val filteredDropdownItems: List<DropdownItem> get() = _filteredDropdownItems
    /*val filteredPartyItems by derivedStateOf {
        if (searchQuery.isBlank()) {
            parties
        } else {
            parties.filter {
                it.businessName.contains(searchQuery, ignoreCase = true)
            }
        }
    }*/

    var selectedDropdownItem by mutableStateOf<DropdownItem?>(null)
        private set

    var isPartyLoading by mutableStateOf(false)
    var partyError by mutableStateOf<String?>(null)
    //var filteredDropdownItems by

    // Update updatePartySearch function
    fun updatePartySearch(query: String) {
        searchQuery = query
        _filteredDropdownItems.clear()
        _filteredDropdownItems.addAll(
            if (query.isEmpty()) {
                parties.map { DropdownItem(it.id, it.businessName) }
            } else {
                parties.filter {
                    it.businessName.contains(query, true)
                }.map { DropdownItem(it.id, it.businessName) }
            }
        )
    }

    // Update loadParties function
    private fun loadParties() {
        viewModelScope.launch {
            isPartyLoading = true
            partyError = null
            try {
                val response = invoiceRepository.getParties()
                if (response.isSuccessful) {
                    response.body()?.let { partyResponse ->
                        if (partyResponse.success) {
                            _parties.clear()
                            partyResponse.parties.let { parties ->
                                _parties.addAll(parties)
                                // Initialize filtered list with all parties
                                _filteredDropdownItems.clear()
                                _filteredDropdownItems.addAll(
                                    parties.map { DropdownItem(it.id, it.businessName) }
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isPartyLoading = false
            }
        }
    }
    // --- Add functions to update the filter states ---
    fun updateSelectedDropdownItem(item: DropdownItem?) {
        selectedDropdownItem = item
        val party = item?.toParty(parties)
        //selectedParty = party
        selectedPartyItem = party // ✅ Ensures filteredInvoices gets updated
        Log.d("SHAKIL", selectedDropdownItem?.name ?: "no party selected or name not found")
    }

    fun updateInvoice(
        updatedInvoice: Invoice
    ) {
        Log.d("SHAKIL", updatedInvoice.toString())
        viewModelScope.launch {
            updateLoading = true
            updateError = null
            try {
                Log.d("SHAKIL", "now api will get called")
                val response = invoiceRepository.updateInvoiceById(
                    updatedInvoice.id.toString(),
                    UpdateInvoiceRequest(updatedInvoice.unitPrice.toDouble(), updatedInvoice.quantity, updatedInvoice.date)
                )
                Log.d("SHAKIL", "now api ended... called")
                if (response.isSuccessful) {
                    Log.d("SHAKIL", response.body().toString())
                    // Refresh data
                    showEditSheet = false
                    refresh()
                } else {
                    updateError = response.message()
                }
            } catch (e: Exception) {
                Log.d("SHAKIL", e.toString())
                updateError = e.localizedMessage
            } finally {
                updateLoading = false
            }
        }
    }

    /**
     * Public entry point: kicks off the lookup
     */
    private fun loadSRName(id: Int) {
        viewModelScope.launch {
            _srName.postValue(getSRNameById(id))
        }
    }

    init {
        savedStateHandle.get<String>("invoiceId")?.let { invoiceId ->
            loadInvoice(invoiceId)
        }
        loadParties()
    }

    private fun loadInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = invoiceRepository.getInvoiceDetails(invoiceId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _invoice.value = response.body()?.data
                    loadSRName(_invoice.value?.createdById ?: 0)
                } else {
                    _error.value = response.body()?.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load invoice"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Performs the network call on Dispatchers.IO (via Retrofit’s suspend),
     * then finds the matching SR by ID.
     */
    private suspend fun getSRNameById(id: Int): String? {
        val response = withContext(Dispatchers.IO) {
            invoiceRepository.getSRs()
        }
        if (!response.isSuccessful) return null

        val list = response.body()?.data ?: return null
        return list.firstOrNull { it.id == id }?.name
    }

    fun refresh() {
        savedStateHandle.get<String>("invoiceId")?.let { invoiceId ->
            loadInvoice(invoiceId)
        }
    }

    /*fun updateInvoice(updatedInvoice: Invoice) {
        Log.d("SHAKIL", updatedInvoice.toString())
    }*/
}