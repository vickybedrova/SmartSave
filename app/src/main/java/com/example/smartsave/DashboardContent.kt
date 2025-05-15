package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartsave.model.SimpleTransaction
import kotlin.math.abs

@Composable
fun DashboardContent(transactions: List<SimpleTransaction>) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* handle logout */ }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = colors.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Text("SMARTSAVE OVERVIEW", style = typography.headlineSmall.copy(color = colors.primary))
        Text("Saving plan 3%", style = typography.bodySmall.copy(color = colors.onBackground.copy(alpha = 0.6f)))
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Adjust", Icons.Default.Create)
            ActionButton("Pause", Icons.Default.Close)
            ActionButton("Analytics", Icons.Default.List)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .border(width = 4.dp, color = colors.primary, shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("700 lv", style = typography.headlineSmall)
                Text("- 100 lv after 24 h", style = typography.bodySmall.copy(color = colors.onBackground.copy(alpha = 0.6f)))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Savings", style = typography.bodySmall)
            Text("Interest Rate 2.24", style = typography.bodySmall.copy(color = colors.onBackground.copy(alpha = 0.6f)))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(32.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Withdraw", style = typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("Earned this month", "200 BGN")
            InfoCard("Progress this month", "700 BGN")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Transaction History",
                tint = colors.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Transaction History", style = typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip("All", selected = false)
            FilterChip("Today", selected = true)
            FilterChip("This Week", selected = false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(transactions) { tx ->
                TransactionCard(tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector) {
    Button(
        onClick = { },
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Text(
        text = text,
        color = if (selected) Color.White else Color.Gray,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun TransactionCard(tx: SimpleTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(tx.title, style = MaterialTheme.typography.bodyLarge)
            Text(tx.date, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (tx.savings > 0) "+ ${tx.savings} lv" else "- ${abs(tx.savings)} lv",
                color = if (tx.savings > 0) Color(0xFF00C853) else Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
            Text("${tx.amount} lv", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
        }
    }
}