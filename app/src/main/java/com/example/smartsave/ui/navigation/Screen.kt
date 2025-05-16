package com.example.smartsave.ui.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Setup : Screen("setup")
    object Dashboard : Screen("dashboard")
    object Analytics : Screen("analytics")
    object Withdraw : Screen("withdraw")
}
