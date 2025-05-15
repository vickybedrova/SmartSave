package com.example.smartsave.ui.activity.dashboard

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartsave.model.SimpleTransaction

@Composable
fun DashboardScreen() {
    val dummyTransactions = listOf(
        SimpleTransaction("Transaction", "April 16",140.0,3.50 ),
        SimpleTransaction("Withdrawal", "April 15", 0.0,  -60.0),
        SimpleTransaction("Transaction", "April 14",350.0,8.75, )
    )
    DashboardContent(
        transactions = dummyTransactions
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen()
}