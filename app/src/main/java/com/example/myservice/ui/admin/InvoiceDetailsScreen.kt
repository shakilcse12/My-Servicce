package com.example.myservice.ui.admin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsScreen(navController: NavController, invoiceId: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            Text(
                text = "Invoice ID: ${invoiceId ?: "Unknown"}",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            // Add more invoice details here as needed.
            Text(
                text = "Detailed invoice information goes here...",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
