package com.example.myservice.data.model

import java.time.LocalDate

import com.google.gson.annotations.SerializedName

data class SR(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,  // nullable because JSON can be null

    @SerializedName("avatar")
    val avatar: String?,           // nullable because JSON can be null

    @SerializedName("created_at")
    val createdAt: String,         // ISO 8601 timestamp

    @SerializedName("updated_at")
    val updatedAt: String          // ISO 8601 timestamp
)


data class CollectionScreenState(
    val srs: List<SR>? = emptyList(),
    val selectedSR: SR? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val invoices: List<InvoiceCollectionResponse>? = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInvoiceForCollection: InvoiceCollectionResponse? = null,
    val srsLoading: Boolean = false,
    val srsError: String? = null
)