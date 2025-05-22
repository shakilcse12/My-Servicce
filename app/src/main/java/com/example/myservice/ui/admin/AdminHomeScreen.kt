package com.example.myservice.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myservice.data.model.Party
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.components.DatePickerDialog
import com.example.myservice.ui.components.DateFilterButton
import com.example.myservice.ui.components.SearchablePartyDropdown
import com.example.myservice.viewmodel.AdminViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onInvoiceClick: (String) -> Unit,
    onCreateInvoice: () -> Unit,
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val isAtEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 1 && totalItems > 0
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh() // this will load all the parties and check for new invoices
        onScroll(true)
    }

    LaunchedEffect(listState) {
        var previousOffset = 0
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset == previousOffset) return@collect
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

    Scaffold(
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
                .padding(horizontal = 16.dp, vertical = 0.dp) // consistent horizontal padding
        ) {
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = viewModel.loading)

            FilterSection(
                viewModel = viewModel,
                onStartDateSelected = { showStartDatePicker = true },
                onEndDateSelected = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            )

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

            //Spacer(modifier = Modifier.height(8.dp))

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.checkForNewInvoices() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (viewModel.loading && invoices.isEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                } else {
                    InvoiceList(
                        listState = listState,
                        invoices = viewModel.filteredInvoices,
                        isOwner = true,
                        onDetails = { invoice ->
                            onInvoiceClick(invoice.id.toString())
                        },
                        modifier = Modifier.fillMaxSize()
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
    onEndDateSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(top = 0.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchablePartyDropdown(
            label = "Select Party",
            searchQuery = viewModel.searchQuery,
            onSearchQueryChanged = { viewModel.searchQuery = it },
            items = viewModel.filteredDropdownItems,
            selectedItem = viewModel.selectedDropdownItem,
            onItemSelected = { viewModel.updateSelectedDropdownItem(it) },
            loading = viewModel.isPartyLoading,
            error = viewModel.partyError
        )


        /*PartyDropdown(
            parties = viewModel.parties,
            selectedParty = viewModel.selectedParty,
            onPartySelected = { viewModel.selectedParty = it }
        )*/

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("From", style = MaterialTheme.typography.labelSmall)
                DateFilterButton(
                    label = "Start Date",
                    date = viewModel.selectedStartDate,
                    onClick = onStartDateSelected
                )
            }
            // Middle Column with stats
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Invoice count row
                Text(
                    text = viewModel.filteredInvoices.value.size.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                // Total collection row
                Text(
                    text = "৳${viewModel.filteredInvoices.value.sumOf {
                        it.totalPayableAmount.toDoubleOrNull() ?: 0.0
                    }.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text("To", style = MaterialTheme.typography.labelSmall)
                DateFilterButton(
                    label = "End Date",
                    date = viewModel.selectedEndDate,
                    onClick = onEndDateSelected
                )
            }
        }
    }
}

fun String?.toLocalDate(): LocalDate? = this?.let {
    try {
        LocalDate.parse(it)
    } catch (e: Exception) {
        null
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
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
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = date?.let {
                try {
                    LocalDate.parse(it).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                } catch (e: Exception) {
                    label
                }
            } ?: label
        )
    }
}*/

