package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceCreateResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Invoice?
)
