import com.google.gson.annotations.SerializedName

data class UpdateInvoiceRequest(

    @SerializedName("sales_unit_price")
    val unitPrice: Double,

    @SerializedName("total_count")
    val quantity: Int,

    @SerializedName("date")
    val date: String
)