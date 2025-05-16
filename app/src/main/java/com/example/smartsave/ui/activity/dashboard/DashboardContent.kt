package com.example.smartsave // Ensure this package is correct

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // Import clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // <<< ENSURE THIS IMPORT IS CORRECT
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
// Add other necessary Icons imports if ActionButton uses them e.g.
// import androidx.compose.material.icons.filled.Create
// import androidx.compose.material.icons.filled.Close
// import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter // For ActionButton if using Painter
import androidx.compose.ui.graphics.vector.ImageVector // For ActionButton if using ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.model.Transaction
import com.example.smartsave.ui.activity.dashboard.TransactionFilter // Import your enum
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    transactions: List<Transaction>,
    totalSavings: Double,
    savingsPercentage: Double,
    earnedThisMonthValue: String,
    progressThisMonthValue: String,
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.ExitToApp, // Make sure Icons.Default.ExitToApp is imported
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
            modifier = Modifier.fillMaxWidth(),
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
            InfoCard("Earned this month", earnedThisMonthValue)
            InfoCard("Progress this month", progressThisMonthValue)
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
                contentDescription = "Transaction History Icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterButton("All", selectedFilter == TransactionFilter.ALL) {
                onFilterSelected(TransactionFilter.ALL)
            }
            FilterButton("Today", selectedFilter == TransactionFilter.TODAY) {
                onFilterSelected(TransactionFilter.TODAY)
            }
            FilterButton("This Week", selectedFilter == TransactionFilter.THIS_WEEK) {
                onFilterSelected(TransactionFilter.THIS_WEEK)
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
                val emptyMessage = when (selectedFilter) {
                    TransactionFilter.TODAY -> "No transactions from today."
                    TransactionFilter.THIS_WEEK -> "No transactions from this week."
                    TransactionFilter.ALL -> "No transactions yet."
                }
                Text(
                    emptyMessage,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(
                        items = transactions, // Explicitly name the parameter if compiler is confused
                        key = { transaction -> transaction.id }
                    ) { tx ->
                        TransactionCard(tx)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Ensure these helper Composables are in this file or correctly imported
@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
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