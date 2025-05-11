package com.example.myservice.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myservice.data.model.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun InvoiceList(
    listState: LazyListState,
    invoices: State<List<Invoice>>,
    isOwner: Boolean,
    onPrint: (Invoice) -> Unit = {},
    onEdit: (Invoice) -> Unit = {},
    onDetails: (Invoice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(invoices.value) { invoice ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {

                    // Top row: Business name on left, Date on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = invoice.party.businessName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = LocalDate.parse(invoice.date)
                                .format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amounts
                    Text("Total: ${invoice.totalPayableAmount}")
                    Text("Received: ${invoice.collectedAmount}")

                    if (isOwner) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Button row: Print & Edit on left, Details on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                /*Button(onClick = { onPrint(invoice) }) {
                                    Text("Collection")
                                }
                                Button(onClick = { onEdit(invoice) }) {
                                    Text("Edit")
                                }*/
                            }
                            // Push Details to right
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.End)
                            ) {
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
}