package com.example.myservice.ui.common
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import com.example.myservice.data.model.InvoiceCollectionResponse

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
        topBar = {
            TopAppBar(
                title = { Text("Collection Screen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
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
                viewModel.invoices.isNotEmpty() -> {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .weight(1F),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.invoices) { invoice ->
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
            onConfirm = { amount ->
                viewModel.collectInvoice(invoice.id, amount)
                viewModel.updateSelectedInvoiceForCollection(null)
            }
        )
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
    onConfirm: (Double) -> Unit
) {
    var collectedAmount by remember { mutableStateOf("") }

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
                    value = collectedAmount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            collectedAmount = it
                        }
                    },
                    label = { Text("Collection Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )


            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    collectedAmount.toDoubleOrNull()?.let {
                        if (it > 0) onConfirm(it)
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateFilterButton(
                label = "Start Date",
                date = viewModel.selectedStartDate,
                onClick = onStartDateSelected
            )
            DateFilterButton(
                label = "End Date",
                date = viewModel.selectedEndDate,
                onClick = onEndDateSelected
            )
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
            LocalDate.parse(it).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
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