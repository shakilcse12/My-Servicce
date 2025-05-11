package com.example.myservice.ui.common

// CreateInvoiceScreen.kt
import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.viewmodel.AdminViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.CalendarToday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val viewModel: CreateInvoiceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateInvoiceViewModel(
                    repository = InvoiceRepository(RetrofitInstance.invoiceService)
                ) as T
            }
        }
    )

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Invoice") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Party Dropdown
            DropdownMenuField(
                label = "Select Party",
                items = state.parties,
                selectedItem = state.selectedParty,
                onItemSelected = { viewModel.select(it) },
                loading = state.loadingParties,
                error = state.partiesError
            )

            // Product Dropdown
            DropdownMenuField(
                label = "Select Product",
                items = state.products,
                selectedItem = state.selectedProduct,
                onItemSelected = { viewModel.selectProduct(it) },
                loading = state.loadingProducts,
                error = state.productsError
            )

            NumberInputField(
                label = "Unit Price",
                value = state.unitPrice,
                onValueChange = { viewModel.updateUnitPrice(it) },
                modifier = Modifier.fillMaxWidth()
            )

            NumberInputField(
                label = "Total Count",
                value = state.totalCount,
                onValueChange = { viewModel.updateTotalCount(it) },
                modifier = Modifier.fillMaxWidth()
            )

            NumberInputField(
                label = "Advance amount",
                value = state.collectedMoney,
                onValueChange = { viewModel.updateCollectedMoney(it) },
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                selectedDate = state.date,
                onDateSelected = { viewModel.updateDate(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.createInvoice() },
                enabled = state.isFormValid && !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Invoice")
                }
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class) // Needed for ExposedDropdownMenuBox
@Composable
private fun DropdownMenuField(
    label: String,
    items: List<DropdownItem>,
    selectedItem: DropdownItem?,
    onItemSelected: (DropdownItem) -> Unit,
    loading: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    // Use a Column to place the Box and the error message correctly
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded && !loading, // Menu opens only if not loading
            onExpandedChange = {
                // Let ExposedDropdownMenuBox handle toggling on click,
                // but prevent opening if loading. Dismiss always works.
                if (!loading || expanded) { // Allow closing even if loading started after opening
                    expanded = !expanded
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            // The OutlinedTextField acts as the anchor and display area
            OutlinedTextField(
                value = selectedItem?.name ?: "",
                onValueChange = { /* No op */ }, // Input is read-only
                readOnly = true, // Essential for ExposedDropdownMenuBox to handle clicks
                label = { Text(label) },
                trailingIcon = {
                    if (loading) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } else {
                        // Standard practice: Use ExposedDropdownMenuDefaults.TrailingIcon
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors( // Use specific defaults
                    // You can still customize colors here if needed, e.g.,
                    // disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    // disabledBorderColor = MaterialTheme.colorScheme.outline,
                    // disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // ...etc
                ),
                // **Crucial:** This modifier connects the TextField to the ExposedDropdownMenuBox
                // and allows the box to handle clicks on the TextField area.
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            // This is the actual menu content
            ExposedDropdownMenu(
                expanded = expanded && !loading, // Show menu only if expanded and not loading
                onDismissRequest = { expanded = false }, // Close when clicking outside
                modifier = Modifier
                    // Remove .fillMaxWidth() from menu itself, Box handles width
                    .background(Color.Black) // Keep the black background
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.name,
                                color = Color.White, // Keep white text on black background
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false // Close menu on item selection
                        },
                        // Apply padding provided by ExposedDropdownMenuDefaults
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
                // Handle empty list case inside the menu if desired
                if (!loading && items.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No items available", color = Color.Gray) },
                        onClick = { expanded = false }, // Just dismiss
                        enabled = false // Make it non-interactive
                    )
                }
            }
        } // End ExposedDropdownMenuBox

        // Display error message below the dropdown box
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    } // End Column
}

// Keep other parts of your CreateInvoiceScreen.kt the same
// ... (CreateInvoiceScreen, NumberInputField, DatePickerField, DropdownItem data class) ...

// Keep other parts of your CreateInvoiceScreen.kt the same
// ... (CreateInvoiceScreen, NumberInputField, DatePickerField, DropdownItem data class) ...
/*@Composable
private fun DropdownMenuField(
    label: String,
    items: List<DropdownItem>,
    selectedItem: DropdownItem?,
    onItemSelected: (DropdownItem) -> Unit,
    loading: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            // Wrap the TextField in a Box with clickable modifier
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .background(Color.White)
            ) {
                OutlinedTextField(
                    value = selectedItem?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false, // Disable text field interactions
                    label = { Text(label) },
                    trailingIcon = {
                        if (loading) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown arrow",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = remember { MutableInteractionSource() } // Disable ripple effect
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
                // override the menu background:
                .background(MaterialTheme.colorScheme.surfaceVariant)

        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.name,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}*/

@Composable
private fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.padding(vertical = 8.dp)
    )
}

// Add this import if you don't have it
 // For trailing icon

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
            label = { Text("Date") },
            // Add a trailing icon as a visual cue that it's clickable/interactive
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date"
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
                    onClickLabel = "Select Date",
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

        DatePickerDialog(
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

// Rest of your CreateInvoiceScreen.kt remains the same
// ... (CreateInvoiceScreen, DropdownMenuField, NumberInputField, DropdownItem data class) ...

/*@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
private fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ISO_DATE

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text("Date") },
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable { showDatePicker = true }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val localDate = LocalDate.ofEpochDay(it / 86400000)
                            onDateSelected(localDate.format(dateFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}*/

data class DropdownItem(val id: Int, val name: String)