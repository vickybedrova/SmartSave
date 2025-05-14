package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top-right logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* Handle logout */ }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.Gray
                )
            }
        }

        // Title
        Text(
            text = "ANALYTICS",
            fontSize = 20.sp,
            color = Color(0xFF3D5AFE), // Light blue
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Back + Title + Download
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Text("SmartSave", fontSize = 16.sp, color = Color.Black)
            }

            IconButton(onClick = { /* Handle download */ }) {
                Icon(Icons.Default.MailOutline, contentDescription = "Download", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnalyticsCard(value = "750 lv", label = "Total Saved")
            AnalyticsCard(value = "5 lv", label = "Interest Earned")
            AnalyticsCard(value = "3%", label = "% of Revenue Saved")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { /* Open calendar */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5AFE))
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section titles (just text for now, can later add charts or graphs)
        Text("Monthly Breakdown", fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Savings Growth", fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun AnalyticsCard(value: String, label: String) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .border(1.dp, Color.LightGray, MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = value, fontSize = 16.sp, color = Color.Black)
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AnalyticsPreview() {
    AnalyticsScreen(onBack = {})
}
