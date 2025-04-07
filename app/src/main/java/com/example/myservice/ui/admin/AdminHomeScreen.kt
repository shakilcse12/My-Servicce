package com.example.myservice.ui.admin
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle.Alignment.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myservice.data.model.Invoice
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myservice.data.repository.AuthRepository
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.login.LoginViewModel
import com.example.myservice.ui.navigation.Screen
import com.example.myservice.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

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
            FloatingActionButton(onClick = onCreateInvoice ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Invoice")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                InvoiceList(
                    listState = listState,
                    invoices = invoices,
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