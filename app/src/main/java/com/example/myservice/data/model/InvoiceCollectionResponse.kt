package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceCollectionResponse(
    @SerializedName("party_id")
    val id: Int,

    @SerializedName("party_name")
    val businessName: String,

    @SerializedName("invoice_date")
    val invoiceDate: String,

    @SerializedName("total_invoice_amount")
    val totalInvoiceAmount: String,

    @SerializedName("total_collection_amount")
    val totalCollectionAmount: String,

    @SerializedName("total_collection_amount_approved")
    val totalCollectionAmountApproved: String
)
