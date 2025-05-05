package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class SRCollectionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("error")
    val error: String?,              // or Boolean if you prefer

    @SerializedName("message")
    val message: String?
)
