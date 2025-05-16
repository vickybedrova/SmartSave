package com.example.smartsave.ui.activity

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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartsave.ui.theme.greyFieldBackground

@Composable
fun WithdrawScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var amount by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
            }
            Text(
                text = "SmartSave",
                style = typography.titleSmall.copy(fontSize = 16.sp),
                color = colors.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "WITHDRAW SAVINGS",
            style = typography.headlineSmall,
            color = colors.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Saving plan 3%",
            style = typography.bodySmall,
            color = colors.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(160.dp)
                .border(width = 4.dp, color = colors.primary, shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("700 lv", style = typography.headlineMedium)
                Text("Total Savings", style = typography.bodySmall, color = colors.onBackground.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter Amount",
            style = typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(greyFieldBackground, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, colors.outline, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("BGN", style = typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                singleLine = true,
                placeholder = { Text("0.00", color = colors.onBackground.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .width(160.dp)
                    .height(56.dp),
                textStyle = typography.bodyLarge,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.onBackground.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The money will leave the account in 24 hours",
                style = typography.bodySmall,
                color = colors.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Handle withdraw */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "Withdraw",
                style = typography.labelLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = colors.onPrimary
                )
            )
        }
    }
}