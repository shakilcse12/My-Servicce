package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class InvoiceBySrAndDateRangeReq(
    @SerializedName("user_id")
    val srId: Int,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("end_date")
    val endDate: String
)
