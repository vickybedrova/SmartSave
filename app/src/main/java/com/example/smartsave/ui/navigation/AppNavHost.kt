package com.example.smartsave.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsave.ui.activity.AnalyticsScreen
import com.example.smartsave.ui.activity.WithdrawScreen
import com.example.smartsave.ui.activity.dashboard.DashboardScreen
import com.example.smartsave.ui.activity.login.LoginScreen
import com.example.smartsave.ui.activity.smartSaveSetup.SmartSaveSetupScreen
import com.example.smartsave.ui.activity.welcome.WelcomeScreen

@Composable
fun AppNavHost(startDestination: String = Screen.Welcome.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Setup.route) { SmartSaveSetupScreen(navController) }
        composable(Screen.Analytics.route){ AnalyticsScreen(navController) }
        composable(Screen.Withdraw.route){ WithdrawScreen(navController) }
    }
}
