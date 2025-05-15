package com.example.smartsave.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* Handle logout */ }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = colors.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Text(
            text = "ANALYTICS",
            style = typography.headlineSmall.copy(color = colors.primary),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                }
                Text("SmartSave", style = typography.titleSmall)
            }

            IconButton(onClick = { /* Handle download */ }) {
                Icon(Icons.Default.MailOutline, contentDescription = "Download", tint = colors.onBackground.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnalyticsCard(value = "750 lv", label = "Total Saved")
            AnalyticsCard(value = "5 lv", label = "Interest Earned")
            AnalyticsCard(value = "3%", label = "% of Revenue Saved")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { /* Open calendar */ },
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Monthly Breakdown", style = typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Savings Growth", style = typography.titleMedium)
    }
}

@Composable
fun AnalyticsCard(value: String, label: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, colors.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = value, style = typography.bodyLarge.copy(color = colors.onBackground))
            Text(text = label, style = typography.bodySmall.copy(color = colors.onBackground.copy(alpha = 0.6f)))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsPreview() {
    AnalyticsScreen(onBack = {})
}
