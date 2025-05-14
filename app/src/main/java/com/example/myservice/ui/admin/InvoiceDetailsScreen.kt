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
import androidx.compose.runtime.livedata.observeAsState
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
import formatDate
import formatDateTime
import formatUtcTimestamp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import convertDateFormat
import formatDate
import formatDateTime
import formatUtcTimestamp

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
                return SingleInvoiceViewModel(handle, InvoiceRepository(RetrofitInstance.invoiceService)) as T
            }
        }
    )

    val invoice by viewModel.invoice
    val loading by viewModel.loading
    val error by viewModel.error
    val srName by viewModel.srName.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
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
            InfoRow("Created At", formatDateTime(invoice.createdAt))
            InfoRow("Created By (SR)", srName ?: "Loading...")
        }

        SectionCard(title = "Customer") {
            InfoRow("Business", invoice.party?.businessName ?: "-")
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

        SectionCard(title = "Payment") {
            InfoRow("Collected", "৳${invoice.collectedAmount}")
            InfoRow("Remaining", "৳${invoice.remainingAmount}")
        }

        SectionCard(
            title = "Timestamps",
            background = MaterialTheme.colorScheme.secondaryContainer
        ) {
            InfoRow("Last Updated", formatDateTime(invoice.updatedAt))
            InfoRow("UTC Logged", formatUtcTimestamp(invoice.updatedAt))
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
