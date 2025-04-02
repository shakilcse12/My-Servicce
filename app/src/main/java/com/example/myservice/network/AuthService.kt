package com.example.myservice.network

import CreateInvoiceRequest
import com.example.myservice.data.constants.Api
import com.example.myservice.data.model.AuthResponse
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.InvoiceResponse
import com.example.myservice.data.model.Party
import com.example.myservice.data.model.Product
import retrofit2.http.*
import retrofit2.Response

interface AuthService {
    @POST(Api.Endpoints.LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
}

// InvoiceService.kt
interface InvoiceService {
    @GET(Api.Endpoints.INVOICES)
    suspend fun getAllInvoices(): Response<InvoiceResponse>

    /*@GET("invoices/{id}")
    suspend fun getInvoice(@Path("id") invoiceId: String): Response<Invoice>

    @POST("invoices")
    suspend fun createInvoice(@Body invoice: CreateInvoiceRequest): Response<Invoice>

    @PUT("invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") invoiceId: String,
        @Body invoice: AuthResponse
    ): Response<Invoice>
     */
}

// ProductService.kt
interface ProductService {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
}

// PartyService.kt
interface PartyService {
    @GET("parties")
    suspend fun getParties(): Response<List<Party>>

    @POST("parties")
    suspend fun createParty(@Body party: Party): Response<Party>
}

data class LoginRequest(
    val email: String,
    val password: String
)