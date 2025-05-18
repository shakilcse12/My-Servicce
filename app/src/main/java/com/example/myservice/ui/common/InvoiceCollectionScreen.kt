package com.example.myservice.ui.common
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.model.SR
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.admin.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myservice.viewmodel.InvoiceCollection
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.wear.compose.material.ContentAlpha
import com.example.myservice.data.model.InvoiceCollectionResponse
import com.example.myservice.ui.components.DatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCollectionScreen(
    onBack: () -> Unit,
    viewModel: InvoiceCollection = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceCollection(
                    repository = InvoiceRepository(RetrofitInstance.invoiceService)
                ) as T
            }
        }
    )
) {
    //val state by viewModel.uiState.collectAsState()
    val srs = viewModel.srs
    val invoices = viewModel.invoices
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Load invoices when filters change
    LaunchedEffect(viewModel.selectedSR, viewModel.selectedStartDate, viewModel.selectedEndDate) {
        if (viewModel.selectedSR != null &&
            viewModel.selectedStartDate != null &&
            viewModel.selectedEndDate != null) {
            viewModel.loadInvoices()
        }
    }

    val toastMessage by viewModel.toastMessage.collectAsState()

    // Show toast when toastMessage is not null
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Scaffold(

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // Replace PartySearchField with
            PartySearchWithDropdown(viewModel)
            // Filter Section
            FilterSection(
                viewModel = viewModel,
                onStartDateSelected = { showStartDatePicker = true },
                onEndDateSelected = { showEndDatePicker = true }
            )
            // Date Pickers
            com.example.myservice.ui.components.DatePickerDialog(
                showDialog = showStartDatePicker,
                initialDate = viewModel.selectedStartDate?.toLocalDate() ?: LocalDate.now(),
                onDateSelected = {
                    viewModel.updateSelectedStartDate(it.toString())
                    showStartDatePicker = false
                },
                onDismiss = { showStartDatePicker = false }
            )

            com.example.myservice.ui.components.DatePickerDialog(
                showDialog = showEndDatePicker,
                initialDate = viewModel.selectedEndDate?.toLocalDate() ?: LocalDate.now(),
                onDateSelected = {
                    viewModel.updateSelectedEndDate(it.toString())
                    showEndDatePicker = false
                },
                onDismiss = { showEndDatePicker = false }
            )

            Spacer(Modifier.height(8.dp))
            // Update the when block in InvoiceCollectionScreen
            when {
                viewModel.loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                viewModel.filteredInvoices.isNotEmpty() -> {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .weight(1F),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.filteredInvoices) { invoice ->
                                InvoiceItem(
                                    invoice = invoice,
                                    onCollect = { viewModel.updateSelectedInvoiceForCollection(invoice) }
                                )
                            }
                        }

                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f), // Add weight modifier
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No invoices found")
                    }
                }
            }
        }
    }

    // Collection Dialog
    viewModel.selectedInvoiceForCollection?.let { invoice ->
        CollectionDialog(
            invoice = invoice,
            onDismiss = { viewModel.updateSelectedInvoiceForCollection(null) },
            onConfirm = { amount, date ->
                viewModel.collectInvoice(invoice.id, amount, date)
                viewModel.updateSelectedInvoiceForCollection(null)
            }
        )
    }
}

// New PartySearchWithDropdown composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartySearchWithDropdown(viewModel: InvoiceCollection) {
    val filteredParties = remember(viewModel.partySearchQuery, viewModel.availableParties) {
        if (viewModel.partySearchQuery.isEmpty()) {
            viewModel.availableParties
        } else {
            viewModel.availableParties.filter { party ->
                party.contains(viewModel.partySearchQuery, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = viewModel.isPartyDropdownExpanded,
        onExpandedChange = { viewModel.isPartyDropdownExpanded = it }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(horizontal = 16.dp),
            value = viewModel.partySearchQuery,
            onValueChange = {
                viewModel.partySearchQuery = it
                viewModel.isPartyDropdownExpanded = true
            },
            label = { Text("Search Party") },
            trailingIcon = {
                Row {
                    // Clear button when there's text
                    if (viewModel.partySearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.clearSearch()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = viewModel.isPartyDropdownExpanded
                    )
                }
            }
        )

        ExposedDropdownMenu(
            expanded = viewModel.isPartyDropdownExpanded,
            onDismissRequest = { viewModel.isPartyDropdownExpanded = false }
        ) {
            if (filteredParties.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching parties") },
                    onClick = { viewModel.isPartyDropdownExpanded = false }
                )
            } else {
                filteredParties.forEach { party ->
                    DropdownMenuItem(
                        text = { Text(party) },
                        onClick = {
                            viewModel.partySearchQuery = party
                            viewModel.isPartyDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

// Updated InvoiceItem composable
@Composable
fun InvoiceItem(invoice: InvoiceCollectionResponse, onCollect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Invoice #${invoice.id}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Business: ${invoice.businessName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Amount: ${invoice.totalInvoiceAmount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onCollect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Collect")
            }
        }
    }
}

// Add this new CollectionDialog composable
@Composable
private fun CollectionDialog(
    invoice: InvoiceCollectionResponse,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: String) -> Unit
) {
        var collectedAmount by remember { mutableStateOf("") }
        var showPicker by remember { mutableStateOf(false) }
        // default to today
        var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Uneditable fields
                OutlinedTextField(
                    value = "Invoice #${invoice.id}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Invoice Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = invoice.businessName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = invoice.totalInvoiceAmount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = invoice.totalCollectionAmount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total Collection Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = collectedAmount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            collectedAmount = it
                        }
                    },
                    label = { Text("Collected Amount now") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    selectedDate = LocalDate.parse(selectedDate)
                        .format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    collectedAmount.toDoubleOrNull()?.let { amt ->
                        if (amt > 0) onConfirm(amt, selectedDate)
                    }
                }
            ) {
                Text("Confirm")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun FilterSection(
    viewModel: InvoiceCollection,
    onStartDateSelected: () -> Unit,
    onEndDateSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Reduced vertical padding
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Party Filter
        SRDropDown(
            srs = viewModel.srs,
            selectedSR = viewModel.selectedSR,
            onSRSelected = { viewModel.selectedSR = it }
        )

        // Date Range Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("From", style = MaterialTheme.typography.bodyMedium)

            DateFilterButton(
                label = "Start Date",
                date = viewModel.selectedStartDate,
                onClick = onStartDateSelected,
            )

            Text("To", style = MaterialTheme.typography.bodyMedium)

            DateFilterButton(
                label = "End Date",
                date = viewModel.selectedEndDate,
                onClick = onEndDateSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
private fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    // Consider making the formatter a remember {} constant if reused often
    val dateFormatter = remember { DateTimeFormatter.ISO_DATE }

    // Use a Box to contain the visual TextField and the clickable overlay
    Box(
        modifier = modifier // Apply the modifier passed to this function here
            .padding(vertical = 8.dp) // Apply padding here if needed, or outside
    ) {
        // 1. The visual OutlinedTextField (non-interactive)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {}, // Not directly editable
            readOnly = true,    // Mark as read-only
            label = { Text("Select Collection Date") },
            // Add a trailing icon as a visual cue that it's clickable/interactive
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Collection Date"
                )
            },
            modifier = Modifier.fillMaxWidth(), // Fill width within the Box
            // Prevent the TextField itself from handling interactions or showing ripple
            interactionSource = remember { MutableInteractionSource() }
            // Optional: Customize colors for readOnly state if needed
        )

        // 2. Transparent Clickable Overlay
        // This Box sits on top of the OutlinedTextField
        Box(
            modifier = Modifier
                .matchParentSize() // Makes this Box cover the OutlinedTextField
                .clickable(
                    // Indicate the purpose of the click clearly
                    onClickLabel = "Select Collection Date",
                    onClick = { showDatePicker = true }, // Action to show the dialog
                    // Disable ripple effect for the transparent overlay itself
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
        )
    } // End of container Box

    // --- Date Picker Dialog Logic (remains mostly the same) ---
    if (showDatePicker) {
        // Remember state for the DatePicker Dialog
        val datePickerState = rememberDatePickerState(
            // Optionally initialize with the currently selected date
            initialSelectedDateMillis = try {
                if (selectedDate.isNotEmpty()) {
                    LocalDate.parse(selectedDate, dateFormatter)
                        .atStartOfDay(java.time.ZoneOffset.UTC) // Use UTC or system default ZoneId
                        .toInstant()
                        .toEpochMilli()
                } else null
            } catch (e: Exception) { null /* Handle parse error */ }
        )

        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Get selected millis, default to current date if null? Optional.
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            // Convert millis to LocalDate using UTC epoch day
                            val localDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                .atZone(java.time.ZoneOffset.UTC) // Use UTC Zone
                                .toLocalDate()
                            onDateSelected(localDate.format(dateFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            // Add a dismiss button for better UX
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SRDropDown(
    srs: List<SR>,
    selectedSR: SR?,
    onSRSelected: (SR?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedSR?.name ?: "Select a SR",
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Select SR") },
                onClick = {
                    onSRSelected(null)
                    expanded = false
                }
            )
            srs.forEach { sr ->
                DropdownMenuItem(
                    text = { Text(sr.name) },
                    onClick = {
                        onSRSelected(sr)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateFilterButton(
    label: String,
    date: String?,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick
    ) {
        Text(text = date?.let {
            LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
        } ?: label)
    }
}

// Extension function for String to LocalDate conversion
fun String?.toLocalDate(): LocalDate? = this?.let {
    try {
        LocalDate.parse(it)
    } catch (e: Exception) {
        null
    }
}

// Add this for currency formatting
class NumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(text.text.formatAsCurrency()),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

fun String.formatAsCurrency(): String {
    return if (isNotEmpty()) "₹$this" else ""
}