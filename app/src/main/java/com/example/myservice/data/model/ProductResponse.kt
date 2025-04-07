package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("products")
    val products: List<Product>
)
