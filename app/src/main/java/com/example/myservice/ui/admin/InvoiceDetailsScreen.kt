package com.example.myservice.ui.admin
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myservice.data.model.Invoice
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.viewmodel.SingleInvoiceViewModel
import convertDateFormat
import formatDateTime
import formatUtcTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(navController: NavController,
                         invoiceId: String) {

    // Initialize ViewModel with proper factory
    val viewModel: SingleInvoiceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val savedStateHandle = SavedStateHandle().apply {
                    set("invoiceId", invoiceId)
                }
                return SingleInvoiceViewModel(
                    savedStateHandle,
                    InvoiceRepository(RetrofitInstance.invoiceService)
                ) as T
            }
        }
    )

    // Observe ViewModel state
    val invoice by viewModel.invoice
    val loading by viewModel.loading
    val error by viewModel.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> FullScreenLoader()
            error != null -> ErrorMessage(error!!) { viewModel.refresh() }
            invoice != null -> InvoiceContent(invoice!!, padding)
            else -> ErrorMessage("Invoice not found") { viewModel.refresh() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceContent(invoice: Invoice, padding: PaddingValues) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Invoice ID: ${invoice.invoiceId}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customer Info", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Business: ${invoice.party?.businessName}")
                    Text("Owner: ${invoice.party?.ownerName}")
                    Text("Address: ${invoice.party?.officeAddress}")
                    Text("Phone No: ${invoice.party?.phoneNo}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product Info", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Product Name: ${invoice.product?.name}")
                    Text("Unit Price: ৳${invoice.unitPrice}")
                    Text("Quantity: ${invoice.quantity}")
                    Text("Total Payable: ৳${invoice.totalPayableAmount}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Info", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Collected: ৳${invoice.collectedAmount}")
                    Text("Remaining: ৳${invoice.remainingAmount}")
                    Text("Created At: ${formatDateTime(invoice.createdAt)}")
                    Text("Created By (User ID): ${invoice.createdById}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Invoice Date: ${(invoice.date)}")
                    Text("Last Updated: ${formatDateTime(invoice.updatedAt.toString())}")
                }
            }
        }
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
