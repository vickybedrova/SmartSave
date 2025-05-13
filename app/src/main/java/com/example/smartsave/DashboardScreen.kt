package com.example.smartsave

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.smartsave.domain.logic.DashboardService
import com.example.smartsave.domain.logic.SmartSaveCalculator
import com.example.smartsave.data.MyPosTransactionService
import com.example.smartsave.model.DashboardState
import com.example.smartsave.DashboardContent


@Composable
fun DashboardScreen() {
    var dashboardState by remember { mutableStateOf<DashboardState?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val dashboardService = DashboardService(
            MyPosTransactionService(),
            SmartSaveCalculator(5.0, 1.5)
        )

        dashboardService.getDashboardDataForCurrentMonth(object : DashboardService.DashboardCallback {
            override fun onSuccess(state: DashboardState) {
                dashboardState = state
            }

            override fun onFailure(t: Throwable) {
                errorMessage = "Failed to load data: ${t.message}"
            }
        })
    }

    when {
        errorMessage != null -> {
            Text("Error: $errorMessage", color = Color.Red)
        }
        dashboardState == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            DashboardContent(state = dashboardState!!)
        }
    }
}

