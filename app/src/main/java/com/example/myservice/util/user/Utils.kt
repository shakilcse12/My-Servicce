import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

fun formatUtcTimestamp(isoString: String): String {
    val instant = Instant.parse(isoString)
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun formatDateTime(input: String): String {
    val inputFormatter = DateTimeFormatter.ISO_INSTANT
    val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    return try {
        val instant = Instant.parse(input)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        zonedDateTime.format(outputFormatter)
    } catch (e: Exception) {
        "Invalid date format"
    }
}