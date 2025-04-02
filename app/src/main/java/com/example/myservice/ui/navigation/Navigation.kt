package com.example.myservice.ui.navigation

import AdminHomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myservice.ui.login.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SalesRepHome : Screen("sales_rep_home")
    object CreateInvoice : Screen("create_invoice")
    object AdminHome : Screen("admin_home")
    object EditInvoice : Screen("edit_invoice/{invoiceId}") {
        fun createRoute(invoiceId: String) = "edit_invoice/$invoiceId"
    }
}

@Composable
fun MyServiceAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) { LoginScreen(onLoginSuccess = {
            navController.navigate(Screen.AdminHome.route)
        }) }
        //composable(Screen.SalesRepHome.route) { SalesRepHomeScreen(navController) }
        //composable(Screen.CreateInvoice.route) { CreateInvoiceScreen(navController) }
        composable(Screen.AdminHome.route) { AdminHomeScreen(navController) }
        composable(
            route = Screen.EditInvoice.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            //EditInvoiceScreen(navController, invoiceId)
        }
    }
}