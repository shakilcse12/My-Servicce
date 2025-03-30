package com.example.myservice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.myservice.ui.dashboard.DashboardScreen
import com.example.myservice.ui.login.LoginScreen

@Composable
fun MyServiceAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("login")
                }
            )
        }
        composable("dashboard") {
            //LoginScreen()
        }
    }
}