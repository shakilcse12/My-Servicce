package com.example.myservice.network

import CreateInvoiceRequest
import com.example.myservice.data.constants.Api
import com.example.myservice.data.model.AuthResponse
import com.example.myservice.data.model.CollectionResponse
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.InvoiceCreateResponse
import com.example.myservice.data.model.InvoiceResponse
import com.example.myservice.data.model.InvoiceResponseSingle
import com.example.myservice.data.model.Party
import com.example.myservice.data.model.PartyResponse
import com.example.myservice.data.model.Product
import com.example.myservice.data.model.ProductResponse
import retrofit2.http.*
import retrofit2.Response
import com.example.myservice.data.model.SR;
import com.example.myservice.data.model.InvoiceBySrAndDateRangeReq;
import com.example.myservice.data.model.InvoiceCollectionResponse
import com.example.myservice.data.model.SrListResponse

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

    @GET(Api.Endpoints.INVOICE_DETAILS)
    suspend fun getInvoiceDetailsById(@Path("invoiceId") invoiceId: String) : Response<InvoiceResponseSingle>  // Add Path parameter annotation): Response<InvoiceResponseSingle>

    @POST(Api.Endpoints.INVOICE_CREATE)
    suspend fun createInvoice(@Body invoice: CreateInvoiceRequest): Response<InvoiceCreateResponse>

    @GET(Api.Endpoints.PRODUCTS)
    suspend fun getProducts(): Response<ProductResponse>

    @GET(Api.Endpoints.PARTIES)
    suspend fun getParties(): Response<PartyResponse>

    @GET(Api.Endpoints.SR)
    suspend fun getSalesRepresentatives(): Response<SrListResponse>

    @POST(Api.Endpoints.INVOICE_BY_SR)
    suspend fun getInvoicesBySrAndDateRange(@Body invoiceBySrAndDateRangeReq: InvoiceBySrAndDateRangeReq): Response<CollectionResponse>


    /*@GET("invoices/{id}")
    suspend fun getInvoice(@Path("id") invoiceId: String): Response<Invoice>

    @PUT("invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") invoiceId: String,
        @Body invoice: AuthResponse
    ): Response<Invoice>
     */
}

// ProductService.kt
interface ProductService {

}

// PartyService.kt
interface PartyService {
    @POST(Api.Endpoints.PARTIES)
    suspend fun createParty(@Body party: Party): Response<Party>
}

data class LoginRequest(
    val email: String,
    val password: String
)