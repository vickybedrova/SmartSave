package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this is androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Keep for specific custom colors if needed, but prefer theme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.model.Transaction // <<< IMPORT YOUR NEW JAVA Transaction CLASS
import java.util.Locale // For String.format Locale if needed

// import kotlin.math.abs // Not needed anymore if using getSavingsImpactForList

@OptIn(ExperimentalMaterial3Api::class) // Add if you use experimental components like TopAppBar
@Composable
fun DashboardContent(
    transactions: List<Transaction>, // <<< USE THE NEW Transaction TYPE
    totalSavings: Double,
    savingsPercentage: Double,
    isLoading: Boolean,
    errorMessage: String?,
    onLogout: () -> Unit,
    onWithdrawClicked: () -> Unit
    // Add other callbacks here if your ActionButtons need them directly
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme color
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top-right logout button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onLogout) { // <<< USE THE PASSED LAMBDA
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                )
            }
        }

        // Title
        Text(
            "SMARTSAVE OVERVIEW",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary, // Use theme color
            fontWeight = FontWeight.Bold
        )
        Text(
            "Saving plan: ${String.format("%.0f", savingsPercentage)}%", // Display dynamic percentage
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Pass onClick handlers to ActionButton
            ActionButton("Adjust", Icons.Default.Create) { /* TODO: Implement Adjust action */ }
            ActionButton("Pause", Icons.Default.Close) { /* TODO: Implement Pause action */ }
            ActionButton("Analytics", Icons.Default.List) { /* TODO: Navigate to Analytics */ }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary, // Use theme color
                    shape = CircleShape
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    // Use Locale.US for consistent decimal formatting if needed, or system default
                    String.format(Locale.getDefault(), "%.2f %s", totalSavings, transactions.firstOrNull()?.currency ?: "EUR"),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface, // Use theme color
                    fontWeight = FontWeight.Bold
                )
                // Text("- 100 lv after 24 h", fontSize = 14.sp, color = Color.Gray) // This was a placeholder
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Savings", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Interest Rate (Example: 2.24%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Make dynamic
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onWithdrawClicked) { // <<< USE THE PASSED LAMBDA
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Withdraw")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Withdraw")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // These values will need to be calculated based on transactions eventually
            InfoCard("Earned this month", "0.00 EUR")
            InfoCard("Progress this month", "0.00 EUR")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Transaction History",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transaction History",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface, // Use theme color
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("All", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Today",
                color = MaterialTheme.colorScheme.onPrimary, // Use theme color
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) // Use theme color
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Text("This Week", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Handle loading, error, and empty states
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            }
            errorMessage != null -> {
                Text(
                    "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error, // Use theme color
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            transactions.isEmpty() && !isLoading -> { // Show only if not loading and truly empty
                Text(
                    "No transactions yet.",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(transactions, key = { transaction -> transaction.id }) { tx -> // Use tx.id as key
                        TransactionCard(tx) // Pass the new Transaction object
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) { // <<< ADD onClick LAMBDA
    Button(
        onClick = onClick, // <<< USE THE PASSED LAMBDA
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Use theme color
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onPrimary) // Use theme color
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = MaterialTheme.colorScheme.onPrimary) // Use theme color
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .widthIn(min = 140.dp) // More flexible width
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium) // Use theme color
            .padding(12.dp)
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Use theme color
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) // Use theme color
    }
}

@Composable
fun TransactionCard(tx: Transaction) { // <<< USE THE NEW Transaction TYPE
    val savingsImpactText = tx.getSavingsImpactForList() // Call Java method
    val savingsColor = when {
        savingsImpactText.startsWith("+") -> Color(0xFF2E7D32) // Consider defining these in your theme or as constants
        savingsImpactText.startsWith("-") -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium) // Use theme color
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                tx.description, // Calls tx.getDescription()
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface // Use theme color
            )
            Text(
                tx.getFormattedDate("dd MMM, HH:mm"), // Calls tx.getFormattedDate()
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
            )
            // Optionally display the transaction type for clarity
            // Text(tx.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
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
                text = tx.getDisplayAmountForList(), // Calls tx.getDisplayAmountForList()
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                fontSize = if (savingsImpactText.isNotBlank()) 12.sp else 14.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

