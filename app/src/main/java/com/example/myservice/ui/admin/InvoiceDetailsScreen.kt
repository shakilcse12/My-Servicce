package com.example.myservice.ui.admin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.viewmodel.SingleInvoiceViewModel
import formatDate
import formatDateTime
import formatUtcTimestamp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.res.painterResource
import com.example.myservice.R
import java.time.LocalDate
import com.example.myservice.ui.components.DatePickerDialog
import convertDateFormat
import java.time.format.DateTimeFormatter
import com.example.myservice.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(
    navController: NavController,
    invoiceId: String
) {
    // — ViewModel setup —
    val viewModel: SingleInvoiceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val handle = SavedStateHandle().apply { set("invoiceId", invoiceId) }
                return SingleInvoiceViewModel(
                    handle,
                    InvoiceRepository(RetrofitInstance.invoiceService)
                ) as T
            }
        }
    )

    val invoice by viewModel.invoice
    val loading by viewModel.loading
    val error by viewModel.error
    val srName by viewModel.srName.observeAsState()
    var showEditSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditSheet = true },
                        enabled = invoice != null
                    ) {
                        Icon(Icons.Default.Edit, "Edit Invoice")
                    }
                }
            )
        }
    ) { insets ->
        Box(modifier = Modifier.padding(insets)) {
            when {
                loading -> FullScreenLoader()
                error != null -> ErrorMessage(error!!) { viewModel.refresh() }
                invoice != null -> InvoiceDetailContent(invoice!!, srName)
                else -> ErrorMessage("Invoice not found") { viewModel.refresh() }
            }
        }
        // Now *after* the Scaffold, conditionally emit the bottom sheet
        if (showEditSheet) {
            LaunchedEffect(Unit) {
                sheetState.show()
            }
            ModalBottomSheet(
                onDismissRequest = { showEditSheet = false },
                sheetState = sheetState,
                dragHandle = null, // or provide your own handle
                modifier = Modifier.fillMaxWidth()
            ) {
                EditInvoiceSheet(
                    viewModel = viewModel,
                    invoice = invoice,
                    onDismiss = { showEditSheet = false },
                    onSave = { updated ->
                        // first close sheet, then update
                        showEditSheet = false
                        viewModel.updateInvoice(updated)
                    }
                )
            }
        }
    }
}

@Composable
private fun InvoiceDetailContent(invoice: Invoice, srName: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SectionCard(title = "Invoice Info") {
            InfoRow("Invoice ID", invoice.invoiceId.toString())
            formatDate(invoice.date)?.let { InfoRow("Invoice Date", it) }
            InfoRow("Created By (SR)", srName ?: "Loading...")
        }

        SectionCard(title = "Party") {
            InfoRow("Name", invoice.party?.businessName ?: "-")
            InfoRow("Owner", invoice.party?.ownerName ?: "-")
            InfoRow("Address", invoice.party?.officeAddress ?: "-")
            InfoRow("Phone", invoice.party?.phoneNo ?: "-")
        }

        SectionCard(title = "Product") {
            InfoRow("Name", invoice.product?.name ?: "-")
            InfoRow("Unit Price", "৳${invoice.unitPrice}")
            InfoRow("Quantity", invoice.quantity.toString())
            InfoRow("Total Payable", "৳${invoice.totalPayableAmount}")
        }

        SectionCard(title = "Collection Info") {
            InfoRow("Collected", "৳${invoice.collectedAmount}")
            InfoRow("Remaining", "৳${invoice.remainingAmount}")
        }

        SectionCard(
            title = "Timestamps",
            background = MaterialTheme.colorScheme.secondaryContainer
        ) {
            InfoRow("Created At", formatDateTime(invoice.createdAt))
            InfoRow("Last Updated", formatDateTime(invoice.updatedAt))
            //InfoRow("UTC Logged", formatUtcTimestamp(invoice.updatedAt))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    background: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ColumnScope.InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun FullScreenLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceSheet(
    viewModel: SingleInvoiceViewModel,
    invoice: Invoice?,
    onDismiss: () -> Unit,
    onSave: (Invoice) -> Unit
) {
    // Local editable state
    var businessName by remember { mutableStateOf(invoice?.party?.businessName.orEmpty()) }
    var unitPrice by remember { mutableStateOf(invoice?.unitPrice.toString()) }
    var quantity by remember { mutableStateOf(invoice?.quantity.toString()) }
    var collectedAmount by remember { mutableStateOf(invoice?.collectedAmount.toString()) }
    var invoiceDate by remember { mutableStateOf(invoice?.date ?: LocalDate.now().toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Edit Invoice",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        OutlinedTextField(
            value = unitPrice,
            onValueChange = { unitPrice = it },
            label = { Text("Unit Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("৳") }
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        /*OutlinedTextField(
            value = collectedAmount,
            onValueChange = { collectedAmount = it },
            label = { Text("Collected Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("৳") }
        )*/

        DatePickerField(
            labelText = "Select Invoice Date",
            selectedDate = LocalDate.parse(invoiceDate)
                .format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
            onDateSelected = { invoiceDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        viewModel.updateError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Save button at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    onSave(
                        invoice?.copy(
                            party = invoice.party.copy(businessName = businessName),
                            unitPrice = (unitPrice.toDoubleOrNull() ?: invoice.unitPrice).toString(),
                            quantity = quantity.toIntOrNull() ?: invoice.quantity,
                            collectedAmount = collectedAmount.toDoubleOrNull().toString() ?: invoice.collectedAmount,
                            date = invoiceDate
                        )!!
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.updateLoading
            ) {
                if (viewModel.updateLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceSheet2(
    invoice: Invoice?,
    onDismiss: () -> Unit,
    onSave: (Invoice) -> Unit
) {
    // Local editable state
    var businessName by remember { mutableStateOf(invoice?.party?.businessName.orEmpty()) }
    var unitPrice by remember { mutableStateOf(invoice?.unitPrice.toString()) }
    var quantity by remember { mutableStateOf(invoice?.quantity.toString()) }
    var collectedAmount by remember { mutableStateOf(invoice?.collectedAmount.toString()) }
    // etc. for other fields…

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header with dismiss icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Edit Invoice",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Divider()

        // Scrollable form
        Column(
            modifier = Modifier
                .weight(1f) // Pin save bar below
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unitPrice,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) unitPrice = it },
                label = { Text("Unit Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.matches(Regex("^\\d*\$"))) quantity = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = collectedAmount,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) collectedAmount = it },
                label = { Text("Collected Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // … other fields …
        }

        // Pinned Save button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                onSave(
                    invoice?.copy(
                        party = invoice.party.copy(businessName = businessName),
                        unitPrice = (unitPrice.toDoubleOrNull() ?: invoice.unitPrice).toString(),
                        quantity = quantity.toIntOrNull() ?: invoice.quantity,
                        collectedAmount = collectedAmount.toDoubleOrNull().toString() ?: invoice.collectedAmount
                        // … map other updated fields …
                    )!!
                )
            }) {
                Text("Save")
            }
        }
    }
}

// Helper extension to parse String → LocalDate
private fun String.toLocalDate(): LocalDate? =
    runCatching { LocalDate.parse(this) }.getOrNull()

