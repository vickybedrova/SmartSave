package com.example.smartsave

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartsave.model.Transaction // Import the correct Transaction model
import com.example.smartsave.ui.theme.SmartSaveTheme // Assuming your theme is here

/*
// THIS COMPOSABLE IS NO LONGER THE MAIN ENTRY POINT FOR THE DASHBOARD UI.
// DashboardActivity's setContent block now directly calls DashboardContent.
// You can remove this entirely if DashboardActivity is your main screen setup.
@Composable
fun DashboardScreen() {
    // If you were to keep this, it would need to fetch data or get it from a ViewModel,
    // and then call DashboardContent correctly. But DashboardActivity already does this.
    // For now, it's best to remove or comment it out to avoid confusion and errors.

    // val dummyTransactions = listOf(
    //    SimpleTransaction("Transaction", "April 16",140.0,3.50 ),
    //    SimpleTransaction("Withdrawal", "April 15", 0.0,  -60.0),
    //    SimpleTransaction("Transaction", "April 14",350.0,8.75, )
    // )
    // DashboardContent( // This call is incorrect and causes the errors
    //    transactions = dummyTransactions
    // )
}
*/

// --- PREVIEWS FOR DashboardContent ---
// These previews now directly call DashboardContent with appropriate dummy data.

@Preview(showBackground = true, name = "Dashboard Loaded")
@Composable
fun PreviewDashboardContentLoaded() { // Renamed for clarity
    // Create dummy data using the new com.example.smartsave.model.Transaction
    val dummyRealTransactions = listOf(
        Transaction("Salary", 2000.0, "INCOME", 200.0, System.currentTimeMillis() - 86400000L * 2, "EUR").apply { id = "preview1" },
        Transaction("Groceries", 75.0, "EXPENSE", 7.5, System.currentTimeMillis() - 86400000L, "EUR").apply { id = "preview2" },
        Transaction("Cash Withdrawal", 150.0, "WITHDRAW", 0.0, System.currentTimeMillis(), "EUR").apply { id = "preview3" }
    )

    SmartSaveTheme {
        DashboardContent(
            transactions = dummyRealTransactions,
            totalSavings = 750.25,
            savingsPercentage = 10.0,
            isLoading = false,
            errorMessage = null,
            onLogout = { Log.d("Preview", "Logout clicked") },
            onWithdrawClicked = { Log.d("Preview", "Withdraw clicked") }
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Loading State")
@Composable
fun PreviewDashboardContentLoading() { // Renamed for clarity
    SmartSaveTheme {
        DashboardContent(
            transactions = emptyList(), // No transactions when loading
            totalSavings = 0.0,
            savingsPercentage = 0.0,
            isLoading = true, // Simulate loading
            errorMessage = null,
            onLogout = { Log.d("Preview", "Logout clicked") },
            onWithdrawClicked = { Log.d("Preview", "Withdraw clicked") }
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Error State")
@Composable
fun PreviewDashboardContentError() { // Renamed for clarity
    SmartSaveTheme {
        DashboardContent(
            transactions = emptyList(),
            totalSavings = 0.0,
            savingsPercentage = 0.0,
            isLoading = false,
            errorMessage = "Could not connect to the server. Please check your internet connection.", // Simulate an error
            onLogout = { Log.d("Preview", "Logout clicked") },
            onWithdrawClicked = { Log.d("Preview", "Withdraw clicked") }
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Empty State")
@Composable
fun PreviewDashboardContentEmpty() { // Renamed for clarity
    SmartSaveTheme {
        DashboardContent(
            transactions = emptyList(), // No transactions
            totalSavings = 120.50,
            savingsPercentage = 5.0,
            isLoading = false,
            errorMessage = null,
            onLogout = { Log.d("Preview", "Logout clicked") },
            onWithdrawClicked = { Log.d("Preview", "Withdraw clicked") }
        )
    }
}