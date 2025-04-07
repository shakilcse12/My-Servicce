package com.example.myservice.ui.common

// CreateInvoiceScreen.kt
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
                label = "Collected Money",
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
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        expanded = false
                    }
                )
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
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
}

data class DropdownItem(val id: Int, val name: String)