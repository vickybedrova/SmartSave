package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.model.Transaction
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    transactions: List<Transaction>,
    totalSavings: Double,
    savingsPercentage: Double,
    earnedThisMonthValue: String,
    progressThisMonthValue: String,
    isLoading: Boolean,
    errorMessage: String?,
    onLogout: () -> Unit,
    onWithdrawClicked: () -> Unit,
    onAdjustClicked: () -> Unit,
    onAnalyticsClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var selectedTab by remember { mutableStateOf("All") }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "SMARTSAVE OVERVIEW",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Saving plan: ${String.format("%.0f", savingsPercentage)}%",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Adjust", painterResource(id = R.drawable.baseline_percent_24), onAdjustClicked)
            ActionButton("Pause", painterResource(id = R.drawable.baseline_pause_24)) { /* TODO */ }
            ActionButton("Analytics", painterResource(id = R.drawable.baseline_bar_chart_24), onAnalyticsClicked)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format(Locale.getDefault(), "%.2f %s", totalSavings, transactions.firstOrNull()?.currency ?: "EUR"),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Savings", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Interest Rate (Example: 2.24%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onWithdrawClicked) {
                Icon(painterResource(id = R.drawable.baseline_transit_enterexit_24), contentDescription = "Withdraw")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Withdraw")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // --- USE THE NEW PARAMETER for "Earned this month" ---
            InfoCard("Earned this month", earnedThisMonthValue)
            InfoCard("Progress this month", progressThisMonthValue) // TODO: Calculate this separately
        }


        Spacer(modifier = Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_history_24),
                contentDescription = "Clock Icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transaction History",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val tabOptions = listOf("All", "Today", "This Week")

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabOptions.forEach { tab ->
                val isSelected = tab == selectedTab
                Text(
                    text = tab,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            }
            errorMessage != null -> {
                Text(
                    "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            transactions.isEmpty() && !isLoading -> {
                Text(
                    "No transactions yet.",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(transactions, key = { transaction -> transaction.id }) { tx ->
                        TransactionCard(tx)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ActionButton(text: String, iconPainter: Painter, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(iconPainter, contentDescription = text, tint = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = MaterialTheme.colorScheme.onPrimary)
    }
}


@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .widthIn(min = 140.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TransactionCard(tx: Transaction) {
    val savingsImpactText = tx.getSavingsImpactForList()
    val savingsColor = when {
        savingsImpactText.startsWith("+") -> Color(0xFF2E7D32)
        savingsImpactText.startsWith("-") -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                tx.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                tx.getFormattedDate(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            if (savingsImpactText.isNotBlank()) {
                Text(
                    text = savingsImpactText,
                    color = savingsColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = tx.getDisplayAmountForList(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (savingsImpactText.isNotBlank()) 12.sp else 14.sp,
                textAlign = TextAlign.End
            )
        }
    }
}