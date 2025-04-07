package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("product_type_id")
    val productTypeId: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("product_type")
    val productType: ProductType
)

data class ProductType(
    @SerializedName("id")
    val id: Int,

    @SerializedName("type_name")
    val typeName: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)