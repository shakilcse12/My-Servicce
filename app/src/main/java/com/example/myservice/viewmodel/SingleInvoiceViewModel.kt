package com.example.myservice.viewmodel

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.Recomposer.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.repository.InvoiceRepository
import kotlinx.coroutines.launch

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

    init {
        savedStateHandle.get<String>("invoiceId")?.let { invoiceId ->
            loadInvoice(invoiceId)
        }
    }

    private fun loadInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = invoiceRepository.getInvoiceDetails(invoiceId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _invoice.value = response.body()?.data
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

    fun refresh() {
        savedStateHandle.get<String>("invoiceId")?.let { invoiceId ->
            loadInvoice(invoiceId)
        }
    }
}

/*
class SingleInvoiceViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel(), Parcelable {

    private val _invoice = mutableStateOf<Invoice?>(null)
    val invoice get() = _invoice

    private val _loading = mutableStateOf(false)
    val loading get() = _loading

    private constructor(parcel: Parcel) : this(
        TODO("savedStateHandle"),
        TODO("invoiceRepository")
    ) {
    }

    init {
        val invoiceId = savedStateHandle.get<String>("invoiceId")
        invoiceId?.let { loadInvoice(it) }
    }

    private fun loadInvoice(invoiceId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = invoiceRepository.getInvoiceDetails(invoiceId)
                if (response.isSuccessful) {
                    response.body()?.let { invoiceResponseSingle ->
                        if (invoiceResponseSingle.success) {
                            invoiceResponseSingle.data?.let { data ->
                                _invoice.value = data // Or use nullable type for _invoice
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("InvoiceDetailsVM", "Failed to load invoice", e)
            } finally {
                _loading.value = false
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SingleInvoiceViewModel> {
        override fun createFromParcel(parcel: Parcel): SingleInvoiceViewModel {
            return SingleInvoiceViewModel(parcel)
        }

        override fun newArray(size: Int): Array<SingleInvoiceViewModel?> {
            return arrayOfNulls(size)
        }
    }
}
*/
