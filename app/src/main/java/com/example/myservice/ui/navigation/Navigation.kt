package com.example.myservice.ui.navigation

import ProfileScreen
import RetrofitInstance
import SettingsScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myservice.data.repository.InvoiceRepository
import com.example.myservice.ui.admin.InvoiceDetailsScreen
import com.example.myservice.ui.admin.MainScreen
import com.example.myservice.ui.common.CreateInvoiceScreen
import com.example.myservice.ui.login.LoginScreen
import com.example.myservice.viewmodel.SingleInvoiceViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SalesRepHome : Screen("sales_rep_home")
    data object CreateInvoice : Screen("create_invoice")
    data object CollectionScreen : Screen("collection_screen")
    data object MainScreen : Screen("main_screen")
    data object AdminHome : Screen("admin_home")
    data object Profile : Screen("common_profile")
    data object Settings : Screen("common_settings")
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
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(
                onSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        //composable(Screen.SalesRepHome.route) { SalesRepHomeScreen(navController) }
        //composable(Screen.CreateInvoice.route) { CreateInvoiceScreen(navController) }
        //composable(Screen.AdminHome.route) { AdminHomeScreen(navController) }
        // Instead of directly calling AdminHomeScreen, we now use MainScreen

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

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
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
        val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            InvoiceDetailsScreen(navController = navController, invoiceId.toString())
        }
    }


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