package com.example.myservice.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myservice.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    parentNavController: NavHostController // For root navigation
) {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(

        bottomBar = {
            BottomAppBar(
                actions = {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, "Invoices") },
                        label = { Text("Invoices") },
                        selected = currentRoute == Screen.AdminHome.route,
                        onClick = {
                            childNavController.navigate(Screen.AdminHome.route) {
                                popUpTo(childNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assessment, "Reports") },
                        label = { Text("Reports") },
                        selected = currentRoute == Screen.Reports.route,
                        onClick = {
                            childNavController.navigate(Screen.Reports.route) {
                                popUpTo(childNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = childNavController,
            startDestination = Screen.AdminHome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AdminHome.route) {
                AdminHomeScreen(
                    onInvoiceClick = { invoiceId ->
                        parentNavController.navigate(Screen.InvoiceDetails.createRoute(invoiceId))
                    },
                    onCreateInvoice = { parentNavController.navigate(Screen.CreateInvoice.route) },
                    onLogout = onLogout
                )
            }
            composable(Screen.Reports.route) {
                ReportScreen()
            }
        }
    }
}