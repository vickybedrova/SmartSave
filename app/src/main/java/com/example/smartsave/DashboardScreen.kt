package com.example.smartsave


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.ui.theme.SmartSaveTheme
import kotlin.math.abs



@Composable
fun DashboardScreen() {
    val dummyTransactions = remember {
        listOf(
            TransactionItem("Food Shopping", "July 16", -400.0),
            TransactionItem("Salary Payment", "July 15", 8000.0),
            TransactionItem("Health Expenses", "July 14", -370.0),
            TransactionItem("Freelance Payment", "July 10", 1200.0),
            TransactionItem("House Bills", "July 9", -3100.0)
        )
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

        Text("700€ saved of 2300€ goal", fontSize = 16.sp)
        Text("Expected Return: 5€", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Transactions History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(dummyTransactions) { item ->
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
