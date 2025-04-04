package com.example.myservice.ui.admin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myservice.data.repository.AuthRepository
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.login.LoginViewModel
import com.example.myservice.ui.navigation.Screen
import com.example.myservice.viewmodel.AdminViewModel

// OwnerHomeScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onInvoiceClick: (String) -> Unit,  // For navigation to details
    onCreateInvoice: () -> Unit,       // For FAB navigation
    onLogout: () -> Unit ) {
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
        Box(modifier = Modifier.padding(padding)) {
            if (viewModel.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                InvoiceList(
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
                    }
                )
            }
        }
    }
}