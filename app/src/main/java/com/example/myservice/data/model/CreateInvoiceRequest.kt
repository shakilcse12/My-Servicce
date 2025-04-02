import com.google.gson.annotations.SerializedName

data class CreateInvoiceRequest(
    @SerializedName("partyId")
    val partyId: Int,

    @SerializedName("products_id")
    val productId: Int,

    @SerializedName("sales_unit_price")
    val unitPrice: Double,

    @SerializedName("total_count")
    val quantity: Int,

    @SerializedName("now_collected_money")
    val collectedAmount: Double,

    @SerializedName("date")
    val date: String
)