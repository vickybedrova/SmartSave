package com.example.smartsave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WithdrawScreen(onBack: () -> Unit) {
    var amount by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Back button + Title row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Text("SmartSave", fontSize = 16.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "WITHDRAW SAVINGS",
            fontSize = 20.sp,
            color = Color(0xFF3D5AFE),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Saving plan 3%",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Circle balance box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(160.dp)
                .border(width = 4.dp, color = Color(0xFF3D5AFE), shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("700 lv", fontSize = 22.sp, color = Color.Black)
                Text("Total Savings", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Enter Amount
        Text(
            text = "Enter Amount",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(50.dp)
        ) {
            // Static "BGN" Box
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("BGN", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Input Box
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                singleLine = true,
                placeholder = { Text("0.00") },
                modifier = Modifier
                    .width(160.dp)
                    .height(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Delay Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "The money will leave the account in 24 hours",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Withdraw Button
        Button(
            onClick = { /* Handle withdraw */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5AFE))
        ) {
            Text("Withdraw", color = Color.White, fontSize = 16.sp)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun WithdrawScreenPreview() {
    WithdrawScreen(onBack = {})
}
