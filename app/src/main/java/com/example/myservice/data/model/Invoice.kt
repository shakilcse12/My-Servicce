package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class Invoice(
    @SerializedName("id")
    val id: Int,

    @SerializedName("partyId")
    val partyId: Int,

    @SerializedName("products_id")
    val productId: Int,

    @SerializedName("sales_unit_price")
    val unitPrice: String,

    @SerializedName("total_count")
    val quantity: Int,

    @SerializedName("now_collected_money")
    val collectedAmount: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("created_by_id")
    val createdById: Int,

    @SerializedName("remaining_amount")
    val remainingAmount: String,

    @SerializedName("total_payable_amount")
    val totalPayableAmount: String,

    @SerializedName("invoice_id")
    val invoiceId: String,

    @SerializedName("isUpdatable")
    val isUpdatable: Int,

    val error: String? = null,
)


/*data class Invoice(
    val id: String,
    val partyName: String,
    val totalAmount: Double,
    val receivedAmount: Double
)*/