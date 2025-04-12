package com.example.myservice.ui.admin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.example.myservice.data.model.Party
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.viewmodel.AdminViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import com.example.myservice.ui.components.DatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// OwnerHomeScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onInvoiceClick: (String) -> Unit,  // For navigation to details
    onCreateInvoice: () -> Unit,       // For FAB navigation
    onLogout: () -> Unit,
    onScroll: (Boolean) -> Unit = {},
    bottomBarHeight: Dp
) {
    val viewModel: AdminViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(
                    invoiceRepository = InvoiceRepository(
                        RetrofitInstance.invoiceService
                    )
                ) as T
            }
        }
    )
    val invoices = viewModel.invoices
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var lastScrollPosition by remember { mutableStateOf(0) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Check if we're at the end of the list, but initially false for short lists
    val isAtEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 1 && totalItems > 0
        }
    }
    // Force show bottom bar initially by delaying the check
    LaunchedEffect(Unit) {
        viewModel.checkForNewInvoices()
        onScroll(true) // Force visible on initial load
    }

    // Scroll detection logic, only trigger after first scroll
    // Improved scroll detection logic with debounce
    LaunchedEffect(listState) {
        var previousOffset = 0
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset == previousOffset) return@collect // Skip if no change
                val isScrollingDown = offset > previousOffset
                val shouldShowBottomBar = when {
                    isAtEnd -> false
                    offset == 0 -> true
                    else -> !isScrollingDown
                }
                if (isBottomBarVisible != shouldShowBottomBar) {
                    coroutineScope.launch {
                        isBottomBarVisible = shouldShowBottomBar
                        onScroll(shouldShowBottomBar)
                    }
                }
                previousOffset = offset
            }
    }
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Invoices") },
                actions = {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInvoice) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Invoice")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = viewModel.loading)
            // Filter Section
            FilterSection(
                viewModel = viewModel,
                onStartDateSelected = { showStartDatePicker = true },
                onEndDateSelected = { showEndDatePicker = true }
            )
            // Date Pickers
            DatePickerDialog(
                showDialog = showStartDatePicker,
                initialDate = viewModel.selectedStartDate?.toLocalDate() ?: LocalDate.now(),
                onDateSelected = {
                    viewModel.updateSelectedStartDate(it.toString())
                    showStartDatePicker = false
                },
                onDismiss = { showStartDatePicker = false }
            )

            DatePickerDialog(
                showDialog = showEndDatePicker,
                initialDate = viewModel.selectedEndDate?.toLocalDate() ?: LocalDate.now(),
                onDateSelected = {
                    viewModel.updateSelectedEndDate(it.toString())
                    showEndDatePicker = false
                },
                onDismiss = { showEndDatePicker = false }
            )

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.checkForNewInvoices() }
            ) {
                if (viewModel.loading && invoices.isEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center)) // ✅ Now inside a Box
                    }
                } else {
                    InvoiceList(
                        listState = listState,
                        invoices = viewModel.filteredInvoices,
                        isOwner = true,
                        onPrint = { invoice ->
                            // Print logic
                        },
                        onEdit = { invoice ->
                            //navController.navigate(Screen.EditInvoice.createRoute(invoice.id.toString()))
                        },
                        onDetails = { invoice ->
                            onInvoiceClick(invoice.id.toString())
                        },
                        modifier = Modifier.fillMaxSize()
                        //modifier = Modifier.padding(bottom = bottomBarHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    viewModel: AdminViewModel,
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
        PartyDropdown(
            parties = viewModel.parties,
            selectedParty = viewModel.selectedParty,
            onPartySelected = { viewModel.selectedParty = it }
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
private fun PartyDropdown(
    parties: List<Party>,
    selectedParty: Party?,
    onPartySelected: (Party?) -> Unit
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
            value = selectedParty?.businessName ?: "All Parties",
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Parties") },
                onClick = {
                    onPartySelected(null)
                    expanded = false
                }
            )
            parties.forEach { party ->
                DropdownMenuItem(
                    text = { Text(party.businessName) },
                    onClick = {
                        onPartySelected(party)
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
