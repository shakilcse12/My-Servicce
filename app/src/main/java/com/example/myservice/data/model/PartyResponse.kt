package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class PartyResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("parties")
    val parties: List<Party>
)