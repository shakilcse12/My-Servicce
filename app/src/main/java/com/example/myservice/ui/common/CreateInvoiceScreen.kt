package com.example.myservice.ui.common

// CreateInvoiceScreen.kt
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myservice.data.repository.InvoiceRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myservice.ui.components.DatePickerField
import kotlin.reflect.KFunction1

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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding() // Add this for keyboard handling
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Party Search Field
                SearchablePartyDropdown(
                    label = "Search Party",
                    searchQuery = state.partySearchQuery,
                    onSearchQueryChanged = viewModel::updatePartySearch,
                    items = state.filteredParties,
                    selectedItem = state.selectedParty,
                    onItemSelected = viewModel::select,
                    loading = state.loadingParties,
                    error = state.partiesError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Party Dropdown
                /*DropdownMenuField(
                label = "Select Party",
                items = state.parties,
                selectedItem = state.selectedParty,
                onItemSelected = { viewModel.select(it) },
                loading = state.loadingParties,
                error = state.partiesError
            )*/

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
                    labelText = "Invoice Date",
                    selectedDate = LocalDate.parse(state.date)
                        .format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
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
}

// New composable for searchable dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchablePartyDropdown(
    label: String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    items: List<DropdownItem>,
    selectedItem: DropdownItem?,
    onItemSelected: (DropdownItem?) -> Unit, // Allow null,
    loading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it && !loading },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChanged(it)
                    if (!expanded) expanded = true
                },
                label = { Text(label) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onSearchQueryChanged("")
                                    onItemSelected(null)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded && !loading,
                onDismissRequest = { expanded = false }
            ) {
                if (loading) {
                    DropdownMenuItem(
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        onClick = {}
                    )
                } else {
                    if (items.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No parties found") },
                            onClick = {}
                        )
                    } else {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = item.name,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    onItemSelected(item)
                                    onSearchQueryChanged(item.name)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
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

data class DropdownItem(
    val id: Int,
    val name: String
) {
    companion object {
        val EMPTY = DropdownItem(-1, "") // Optional placeholder
    }
}
