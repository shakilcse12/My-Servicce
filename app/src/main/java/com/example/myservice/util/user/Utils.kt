import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Other update functions (selectParty, selectProduct, etc.)
fun convertDateFormat(input: String): String? {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
        val date = LocalDate.parse(input, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        date.format(outputFormatter)
    } catch (e: Exception) {
        null
    }
}