package com.example.myservice.data.repository

import com.example.myservice.data.model.AuthResponse
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.InvoiceResponse
import com.example.myservice.network.InvoiceService
import retrofit2.Response


class InvoiceRepository(private val invoiceService: InvoiceService) {
    // Return raw Retrofit response instead of Result
    suspend fun getAllInvoices() : Response<InvoiceResponse> {
        return invoiceService.getAllInvoices()
    }
}