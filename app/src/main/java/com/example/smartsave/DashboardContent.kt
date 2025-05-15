package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsave.model.SimpleTransaction

import kotlin.math.abs

@Composable
fun DashboardContent(transactions: List<SimpleTransaction>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* TODO: handle logout */ }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.Gray
                )
            }
        }

        Text("SMARTSAVE OVERVIEW", fontSize = 20.sp, color = Color(0xFF3D5AFE))
        Text("Saving plan 3%", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Adjust", Icons.Default.Create)
            ActionButton("Pause", Icons.Default.Close)
            ActionButton("Analytics", Icons.Default.List)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .border(width = 4.dp, color = Color(0xFF3D5AFE), shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("700 lv", fontSize = 22.sp, color = Color.Black)
                Text("- 100 lv after 24 h", fontSize = 14.sp, color = Color.Gray)
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Savings", fontSize = 12.sp)
            Text("Interest Rate 2.24", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = {}) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Withdraw")
            }
        }



        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("Earned this month", "200 BGN")
            InfoCard("Progress this month", "700 BGN")
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
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transaction History",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("All", color = Color.Gray)
            Text(
                "Today",
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF3D5AFE), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Text("This Week", color = Color.Gray)
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5AFE))
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White)
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .background(Color.White, MaterialTheme.shapes.medium)
            .border(1.dp, Color(0xFF3D5AFE), MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Text(title, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun TransactionCard(tx: SimpleTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(tx.title, fontSize = 14.sp)
            Text(tx.date, fontSize = 12.sp, color = Color.Gray)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (tx.savings > 0) "+ ${tx.savings} lv" else "- ${abs(tx.savings)} lv",
                color = if (tx.savings > 0) Color(0xFF00C853) else Color.Red,
                fontSize = 14.sp
            )
            Text(
                text = "${tx.amount} lv",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }
}
