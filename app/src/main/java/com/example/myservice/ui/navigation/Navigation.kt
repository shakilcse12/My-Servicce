package com.example.myservice.ui.navigation

import RetrofitInstance
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myservice.ui.admin.InvoiceDetailsScreen
import com.example.myservice.ui.admin.MainScreen
import com.example.myservice.ui.login.LoginScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SalesRepHome : Screen("sales_rep_home")
    data object CreateInvoice : Screen("create_invoice")
    data object MainScreen : Screen("main_screen")
    data object AdminHome : Screen("admin_home")
    data object Reports : Screen("report_screen")
    data object EditInvoice : Screen("edit_invoice/{invoiceId}") {
        fun createRoute(invoiceId: String) = "edit_invoice/$invoiceId"
    }
    data object InvoiceDetails : Screen("invoice_details/{invoiceId}") {
        fun createRoute(invoiceId: String) = "invoice_details/$invoiceId"
    }
}

@Composable
fun MyServiceAppNavigation() {
    val navController = rememberNavController()
    val authState = rememberAuthState().value
    NavHost(
        navController = navController,
        startDestination = if (authState) Screen.MainScreen.route else Screen.Login.route
    ) {

        /*composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.AdminHome.route)
            })
        }*/
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        //composable(Screen.SalesRepHome.route) { SalesRepHomeScreen(navController) }
        //composable(Screen.CreateInvoice.route) { CreateInvoiceScreen(navController) }
        //composable(Screen.AdminHome.route) { AdminHomeScreen(navController) }
        // Instead of directly calling AdminHomeScreen, we now use MainScreen
        /*composable(Screen.AdminHome.route) {
            MainScreen()
        }*/

        composable(Screen.MainScreen.route) {
            MainScreen(
                onLogout = {
                    // Clear user session
                    RetrofitInstance.clearToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainScreen.route) { inclusive = true }
                    }
                },
                navController
            )
        }
        composable(
            route = Screen.EditInvoice.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            //EditInvoiceScreen(navController, invoiceId)
        }
        /*composable(
            route = Screen.InvoiceDetails.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            InvoiceDetailsScreen(navController, invoiceId)
        }*/
        composable(
            route = Screen.InvoiceDetails.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
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

// Auth state holder
// In AuthStateViewModel
class AuthStateViewModel : ViewModel() {
    private val _authState = mutableStateOf(RetrofitInstance.getToken() != null)
    val authState: State<Boolean> = _authState

    fun updateAuthState(isAuthenticated: Boolean) {
        _authState.value = isAuthenticated
    }
}

@Composable
fun rememberAuthState(): State<Boolean> {
    val viewModel: AuthStateViewModel = viewModel()
    return viewModel.authState
}