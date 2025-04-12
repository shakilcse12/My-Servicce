package com.example.myservice.data.repository

import CreateInvoiceRequest
import com.example.myservice.data.model.InvoiceCreateResponse
import com.example.myservice.data.model.InvoiceResponse
import com.example.myservice.data.model.InvoiceResponseSingle
import com.example.myservice.data.model.PartyResponse
import com.example.myservice.data.model.ProductResponse
import com.example.myservice.network.InvoiceService
import retrofit2.Response


class InvoiceRepository(private val invoiceService: InvoiceService) {
    // Return raw Retrofit response instead of Result
    suspend fun getAllInvoices() : Response<InvoiceResponse> {
        return invoiceService.getAllInvoices()
    }

    suspend fun getInvoiceDetails(id: String) : Response<InvoiceResponseSingle> {
        return invoiceService.getInvoiceDetailsById(id)
    }

    suspend fun getProducts() : Response<ProductResponse> {
        return invoiceService.getProducts()
    }

    suspend fun getParties() : Response<PartyResponse> {
        return invoiceService.getParties()
    }

    suspend fun createInvoice(createInvoiceRequest: CreateInvoiceRequest) : Response<InvoiceCreateResponse> {
        return invoiceService.createInvoice(createInvoiceRequest)
    }
}