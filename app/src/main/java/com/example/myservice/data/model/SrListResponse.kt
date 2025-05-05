package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class SrListResponse(

    @SerializedName("error")
    val error: String,              // or Boolean if you prefer

    @SerializedName("data")
    val data: List<SR>?
)
