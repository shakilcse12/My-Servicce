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
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myservice.data.repository.AuthRepository
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.login.LoginViewModel
import com.example.myservice.viewmodel.AdminViewModel

// OwnerHomeScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
        topBar = { TopAppBar(title = { Text("All Invoices") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (viewModel.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                InvoiceList(
                    invoices = invoices,
                    isOwner = true,
                    onPrint = { invoice -> /* Print logic */ },
                    onEdit = { invoice -> /* Navigate to edit screen */ }
                )
            }
        }
    }
}

// Common Components
@Composable
private fun InvoiceList(
    invoices: List<Invoice>, // ✅ Ensure invoices is passed correctly
    isOwner: Boolean,
    onPrint: (Invoice) -> Unit = {},
    onEdit: (Invoice) -> Unit = {}
) {
    LazyColumn {
        items(invoices) { invoice -> // ✅ Correct way to iterate over a list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(invoice.partyId.toString(), style = MaterialTheme.typography.titleMedium)
                    Text("Total: ${invoice.totalPayableAmount}")
                    Text("Received: ${invoice.collectedAmount}")
                    if (isOwner) {
                        Row {
                            Button(onClick = { onPrint(invoice) }) { // ✅ Call the provided function
                                Text("Print")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onEdit(invoice) }) { // ✅ Call the provided function
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}

