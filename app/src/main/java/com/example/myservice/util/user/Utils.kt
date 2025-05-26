import com.example.myservice.data.model.Party
import com.example.myservice.ui.common.DropdownItem
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.DateTimeParseException
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

fun formatDate(inputDate: String): String? {
    return try {
        // Parse input date (YYYY-MM-DD)
        val date = LocalDate.parse(inputDate, DateTimeFormatter.ISO_LOCAL_DATE)
        // Format to dd MMM, yyyy
        date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
    } catch (e: DateTimeParseException) {
        // Handle invalid date format
        "Invalid date format"
    }
}

fun formatDateTime(input: String): String {
    val inputFormatter = DateTimeFormatter.ISO_INSTANT
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy 'at' hh:mm a", Locale.getDefault())

    return try {
        val instant = Instant.parse(input)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        zonedDateTime.format(outputFormatter)
    } catch (e: Exception) {
        "Invalid date format"
    }
}

 fun Party.toDropdownItem(): DropdownItem {
    return DropdownItem(id = this.id ?: -1, name = this.businessName ?: "")
}

 fun DropdownItem.toParty(parties : List<Party>): Party? {
    return parties.find { it.id == this.id }
}

// Helper extensions for date handling
fun String?.toLocalDateOrNull(): LocalDate? = try {
    this?.let { LocalDate.parse(it) }
} catch (e: DateTimeParseException) {
    null
}

fun LocalDate?.isInRange(start: LocalDate, end: LocalDate): Boolean {
    return this != null && (this >= start && this <= end)
}