package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class Party(
    @SerializedName("id")
    val id: Int,

    @SerializedName("businessName")
    val businessName: String,

    @SerializedName("officeAddress")
    val officeAddress: String,

    @SerializedName("ownerName")
    val ownerName: String,

    @SerializedName("mobileNo")
    val phoneNo: String,

    @SerializedName("isActive")
    val isActive: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)