package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceCollectionReq(
    @SerializedName("party_id")
    val partyId: Int,

    @SerializedName("collection_amount")
    val collectionAmount: Double,

    @SerializedName("transaction_date")
    val transactionDate: String,

    @SerializedName("collection_date")
    val collectionDate: String
)
