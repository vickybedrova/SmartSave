package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.model.DashboardState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun DashboardContent(state: DashboardState) {
    val transactionItems = state.recentTransactions.map {
        val dateFormat = SimpleDateFormat("MMMM d, HH:mm", Locale.ENGLISH)
        val formattedDate = dateFormat.format(it.date)
        TransactionItem(it.description, formattedDate, it.amount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SmartSave Overview",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("€${"%.2f".format(state.totalSaved)} saved of 2300€ goal", fontSize = 16.sp)
        Text("Expected Return: €${"%.2f".format(state.expectedReturn)}", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Transactions History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(transactionItems) { item ->
                TransactionRow(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionRow(item: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(item.title, fontSize = 16.sp, fontWeight = MaterialTheme.typography.titleSmall.fontWeight)
            Text(item.date, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            text = (if (item.amount > 0) "+€" else "-€") + abs(item.amount),
            color = if (item.amount > 0) Color(0xFF00C853) else Color.Red,
            fontSize = 14.sp
        )
    }
}

data class TransactionItem(val title: String, val date: String, val amount: Double)
