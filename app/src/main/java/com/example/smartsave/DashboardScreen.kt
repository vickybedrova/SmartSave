package com.example.smartsave

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartsave.domain.logic.DashboardService
import com.example.smartsave.domain.logic.SmartSaveCalculator
import com.example.smartsave.data.MyPosTransactionService
import com.example.smartsave.model.DashboardState
import com.example.smartsave.DashboardContent
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
    )}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen()
}