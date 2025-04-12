package com.example.myservice.ui.admin
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myservice.data.model.Invoice

@Composable
fun InvoiceList(
    listState: LazyListState,
    invoices: List<Invoice>,
    isOwner: Boolean,
    onPrint: (Invoice) -> Unit = {},
    onEdit: (Invoice) -> Unit = {},
    onDetails: (Invoice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn (
        state = listState,
        modifier = modifier
            .fillMaxSize() // Take full available space
            .padding(horizontal = 16.dp)
    ) {
        items(invoices) { invoice ->
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Text(invoice.party.businessName, style = MaterialTheme.typography.titleMedium)
                    Text("Total: ${invoice.totalPayableAmount}")
                    Text("Received: ${invoice.collectedAmount}")
                    if (isOwner) {
                        Row {
                            Button(onClick = { onPrint(invoice) }) {
                                Text("Collection")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onEdit(invoice) }) {
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onDetails(invoice) }) {
                                Text("Details")
                            }
                        }
                    }
                }
            }
        }
    }
}
