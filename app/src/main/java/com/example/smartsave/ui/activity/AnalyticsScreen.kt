package com.example.smartsave.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartsave.R

@Composable
fun AnalyticsScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var selectedMonth by remember { mutableStateOf("May") }
    var selectedYear by remember { mutableStateOf("2025") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {

        Text(
            text = "ANALYTICS",
            style = typography.headlineSmall,
            color = colors.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_circle_left_24),
                        contentDescription = "Return"
                    )                }
                Text("SmartSave", style = typography.titleSmall)
            }

            IconButton(onClick = { /* Handle download */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_download_24),
                    contentDescription = "Download"
                )
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
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = { selectedMonth = it },
                onYearSelected = { selectedYear = it }
            )
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
            .background(colors.background, shape = RoundedCornerShape(12.dp))
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

@Composable
fun MonthYearSelector(
    selectedMonth: String,
    selectedYear: String,
    onMonthSelected: (String) -> Unit,
    onYearSelected: (String) -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val years = listOf("2025", "2026")

    var isMonthExpanded by remember { mutableStateOf(false) }
    var isYearExpanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month Dropdown
        Box {
            OutlinedButton(onClick = { isMonthExpanded = true }) {
                Text(selectedMonth)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
            }
            DropdownMenu(
                expanded = isMonthExpanded,
                onDismissRequest = { isMonthExpanded = false },
                modifier = Modifier.heightIn(max = 300.dp) // this allows internal scrolling
            ) {
                months.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthSelected(month)
                            isMonthExpanded = false
                        }
                    )
                }
            }
        }

        // Year Dropdown
        Box {
            OutlinedButton(onClick = { isYearExpanded = true }) {
                Text(selectedYear)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Year")
            }
            DropdownMenu(
                expanded = isYearExpanded,
                onDismissRequest = { isYearExpanded = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            onYearSelected(year)
                            isYearExpanded = false
                        }
                    )
                }
            }
        }
    }
}
