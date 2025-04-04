package com.example.myservice.ui.navigation

import com.example.myservice.ui.admin.AdminHomeScreen
import RetrofitInstance
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myservice.ui.admin.InvoiceDetailsScreen
import com.example.myservice.ui.login.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SalesRepHome : Screen("sales_rep_home")
    object CreateInvoice : Screen("create_invoice")
    object AdminHome : Screen("admin_home")
    object EditInvoice : Screen("edit_invoice/{invoiceId}") {
        fun createRoute(invoiceId: String) = "edit_invoice/$invoiceId"
    }
    object InvoiceDetails : Screen("invoice_details/{invoiceId}") {
        fun createRoute(invoiceId: String) = "invoice_details/$invoiceId"
    }
}

@Composable
fun MyServiceAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = getStartDestination()
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
        composable(
            route = Screen.InvoiceDetails.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            InvoiceDetailsScreen(navController, invoiceId)
        }
    }
}

fun getStartDestination() : String {
    var dest: String? = RetrofitInstance.getToken()

    if (dest != null) {
        Log.d("SHAKIL", dest)
    } else {
        Log.d("SHAKIL", "token null found")
    }
    if(dest != null) return Screen.AdminHome.route
    else return Screen.Login.route
}