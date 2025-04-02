package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Invoice>?
)
