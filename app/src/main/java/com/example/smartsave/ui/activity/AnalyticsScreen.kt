package com.example.smartsave.ui.activity

import android.content.Context
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.navigation.NavController
import com.example.smartsave.R
import java.io.File
import java.io.FileOutputStream
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartsave.ui.theme.blue
import java.text.SimpleDateFormat
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextAlign
import com.example.smartsave.util.SavingsCalculator
import java.util.Calendar
import java.util.Locale

private const val TAG_ANALYTICS_SCREEN = "AnalyticsScreenLogic"

fun monthNameToNumber(monthName: String): Int {
    return try {
        val date = SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName)
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
            cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
        } else {
            -1 // Invalid month name
        }
    } catch (e: Exception) {
        Log.e(TAG_ANALYTICS_SCREEN, "Error parsing month name: $monthName", e)
        -1 // Invalid month name
    }
}

data class ChartDataPoint(val label: String, val value: Float)

@Composable
fun AnalyticsScreen(navController: NavController) { // Removed totalSavings parameter
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val view = LocalView.current


    var selectedMonth by remember { mutableStateOf("May") }
    var selectedYear by remember { mutableStateOf("2025") }

    // State for dynamic total savings
    var totalSavingsValue by remember { mutableStateOf(0.0) }
    var isLoadingTotalSavings by remember { mutableStateOf(true) }
    var totalSavingsErrorMessage by remember { mutableStateOf<String?>(null) }


    // --- NEW State for "Earned from Interest" for selected month ---
    var earnedFromInterestValue by remember { mutableStateOf(0.0) }
    var isLoadingEarnedFromInterest by remember { mutableStateOf(true) }
    var earnedFromInterestErrorMessage by remember { mutableStateOf<String?>(null) }
    var earnedFromInterestCurrency by remember { mutableStateOf("EUR") }

    var earnedThisMonthValue by remember { mutableStateOf(0.0) }
    var isLoadingEarnedThisMonth by remember { mutableStateOf(true) }
    var earnedThisMonthErrorMessage by remember { mutableStateOf<String?>(null) }
    var earnedThisMonthCurrency by remember { mutableStateOf("EUR") }

    var savingsGrowthData by remember { mutableStateOf<List<ChartDataPoint>>(emptyList()) }
    var isLoadingSavingsGrowth by remember { mutableStateOf(true) }
    var savingsGrowthErrorMessage by remember { mutableStateOf<String?>(null) }
    var overallSavingsForChartHeader by remember { mutableStateOf(0.0) } // To display total at top of chart


    var compoundInterestSentence by remember { mutableStateOf<String?>(null) }
    var isLoadingCompoundInterestSentence by remember { mutableStateOf(true) }

    val assumedAnnualInterestRateForProjection = 0.03 // 3%


    LaunchedEffect(key1 = Unit) { // <<<< KEY IS Unit
        isLoadingTotalSavings = true
        totalSavingsErrorMessage = null
        Log.d(
            TAG_ANALYTICS_SCREEN,
            "LaunchedEffect (TotalSavings): Calling recalculateAndUpdatetotalSaved"
        )
        SavingsCalculator.recalculateAndUpdatetotalSaved(object :
            SavingsCalculator.CalculationCallback {
            override fun onSuccess(newTotalSaved: Double) {
                Log.i(TAG_ANALYTICS_SCREEN, "Success (TotalSavings): $newTotalSaved")
                totalSavingsValue = newTotalSaved
                isLoadingTotalSavings = false
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG_ANALYTICS_SCREEN, "Error (TotalSavings): $errorMessage")
                totalSavingsValue = 0.0
                totalSavingsErrorMessage = "Error loading total: $errorMessage"
                isLoadingTotalSavings = false
            }
        })
    }

    LaunchedEffect(key1 = isLoadingTotalSavings) {
        // Only proceed if total savings has finished loading (either success or error)
        if (!isLoadingTotalSavings) {
            if (totalSavingsValue > 0 && totalSavingsErrorMessage == null) {
                // We still set isLoadingCompoundInterestSentence to true here to indicate THIS calculation is starting
                isLoadingCompoundInterestSentence = true
                Log.d(TAG_ANALYTICS_SCREEN, "LaunchedEffect (CompoundProjection): Calculating for total $totalSavingsValue")
                SavingsCalculator.calculateCompoundInterestProjection(
                    totalSavingsValue,
                    assumedAnnualInterestRateForProjection,
                    object : SavingsCalculator.CompoundInterestProjectionCallback {
                        override fun onSuccess(futureValue: Double, interestEarned: Double) {
                            compoundInterestSentence = String.format(
                                Locale.ENGLISH,
                                "Leave %.2f BGN and let them grow. Get back %.2f BGN in just 6 months.",
                                totalSavingsValue,
                                futureValue
                            )
                            Log.i(TAG_ANALYTICS_SCREEN, "Success (CompoundProjection): $compoundInterestSentence")
                            isLoadingCompoundInterestSentence = false // Calculation finished
                        }

                        override fun onError(errorMessage: String) {
                            Log.e(TAG_ANALYTICS_SCREEN, "Error (CompoundProjection): $errorMessage")
                            compoundInterestSentence = "Could not calculate growth projection."
                            isLoadingCompoundInterestSentence = false // Calculation finished (with error)
                        }
                    }
                )
            } else {
                // Total savings is 0, or there was an error loading it, so no projection.
                compoundInterestSentence = null
                isLoadingCompoundInterestSentence = false // Not loading, nothing to project
                Log.d(TAG_ANALYTICS_SCREEN, "Skipping compound projection: totalSavingsValue is 0 or error occurred.")
            }
        }
    }

    // --- LaunchedEffect for "Earned from Interest" (runs when selectedMonth or selectedYear changes) ---
    LaunchedEffect(
        key1 = selectedMonth,
        key2 = selectedYear
    ) { // <<<< KEYS ARE selectedMonth, selectedYear
        isLoadingEarnedFromInterest = true
        earnedFromInterestErrorMessage = null
        Log.d(
            TAG_ANALYTICS_SCREEN,
            "LaunchedEffect (EarnedInterest): Calculating for $selectedMonth $selectedYear"
        )

        val monthNumber = monthNameToNumber(selectedMonth)
        val yearNumber = selectedYear.toIntOrNull()

        if (monthNumber != -1 && yearNumber != null) {
            SavingsCalculator.calculateInterestForSelectedMonth(
                yearNumber,
                monthNumber,
                object : SavingsCalculator.SelectedMonthInterestCallback {
                    override fun onSuccess(totalInterestForMonth: Double, currency: String) {
                        Log.i(
                            TAG_ANALYTICS_SCREEN,
                            "Success (EarnedInterest) for $selectedMonth $selectedYear: $totalInterestForMonth $currency"
                        )
                        earnedFromInterestValue = totalInterestForMonth
                        earnedFromInterestCurrency = currency
                        isLoadingEarnedFromInterest = false
                    }

                    override fun onError(errorMessage: String) {
                        Log.e(
                            TAG_ANALYTICS_SCREEN,
                            "Error (EarnedInterest) for $selectedMonth $selectedYear: $errorMessage"
                        )
                        earnedFromInterestValue = 0.0
                        earnedFromInterestErrorMessage = "Error loading interest: $errorMessage"
                        isLoadingEarnedFromInterest = false
                    }
                }
            )
        } else {
            Log.e(
                TAG_ANALYTICS_SCREEN,
                "Invalid month or year for interest calc: $selectedMonth, $selectedYear"
            )
            earnedFromInterestErrorMessage = "Invalid date selected"
            isLoadingEarnedFromInterest = false
            earnedFromInterestValue = 0.0
        }
    }

    LaunchedEffect(key1 = selectedMonth, key2 = selectedYear) {
        isLoadingEarnedThisMonth = true
        earnedThisMonthErrorMessage = null
        Log.d(
            TAG_ANALYTICS_SCREEN,
            "LaunchedEffect (EarnedThisMonth - Income Savings): Calculating for $selectedMonth $selectedYear"
        )

        val monthNumber = monthNameToNumber(selectedMonth)
        val yearNumber = selectedYear.toIntOrNull()

        if (monthNumber != -1 && yearNumber != null) {
            SavingsCalculator.calculateIncomeSavingsForSelectedMonth(
                yearNumber,
                monthNumber,
                object : SavingsCalculator.SelectedMonthIncomeSavingsCallback {
                    override fun onSuccess(totalIncomeSavingsForMonth: Double, currency: String) {
                        Log.i(
                            TAG_ANALYTICS_SCREEN,
                            "Success (EarnedThisMonth - Income Savings) for $selectedMonth $selectedYear: $totalIncomeSavingsForMonth $currency"
                        )
                        earnedThisMonthValue = totalIncomeSavingsForMonth
                        earnedThisMonthCurrency = currency // Use the currency from this calculation
                        isLoadingEarnedThisMonth = false
                    }

                    override fun onError(errorMessage: String) {
                        Log.e(
                            TAG_ANALYTICS_SCREEN,
                            "Error (EarnedThisMonth - Income Savings) for $selectedMonth $selectedYear: $errorMessage"
                        )
                        earnedThisMonthValue = 0.0
                        earnedThisMonthErrorMessage =
                            "Error loading monthly earnings: $errorMessage"
                        isLoadingEarnedThisMonth = false
                    }
                }
            )
        } else {
            Log.e(
                TAG_ANALYTICS_SCREEN,
                "Invalid month or year for income savings calc: $selectedMonth, $selectedYear"
            )
            earnedThisMonthErrorMessage = "Invalid date selected"
            isLoadingEarnedThisMonth = false
            earnedThisMonthValue = 0.0
        }
    }

    LaunchedEffect(key1 = selectedYear, key2 = selectedMonth) { // Recalculate if month/year changes
        isLoadingSavingsGrowth = true
        savingsGrowthErrorMessage = null
        Log.d(
            TAG_ANALYTICS_SCREEN,
            "LaunchedEffect (SavingsGrowth): Calculating for $selectedMonth $selectedYear"
        )

        val monthNumber = monthNameToNumber(selectedMonth)
        val yearNumber = selectedYear.toIntOrNull()
        val numberOfMonthsForChart = 6 // Or 12, or make it configurable

        if (monthNumber != -1 && yearNumber != null) {
            SavingsCalculator.calculateMonthlySavingsGrowth(
                yearNumber,
                monthNumber,
                numberOfMonthsForChart,
                object : SavingsCalculator.MonthlySavingsGrowthCallback {
                    override fun onSuccess(monthlyData: MutableList<MutableMap<String, Any>>) {
                        Log.i(
                            TAG_ANALYTICS_SCREEN,
                            "Success (SavingsGrowth): Data received - ${monthlyData.size} points"
                        )
                        if (monthlyData.isNotEmpty()) {
                            savingsGrowthData = monthlyData.mapNotNull { dataPoint ->
                                val monthName = dataPoint["monthName"] as? String
                                val savings = (dataPoint["savings"] as? Double)?.toFloat()
                                if (monthName != null && savings != null) {
                                    ChartDataPoint(monthName, savings)
                                } else {
                                    null
                                }
                            }
                            // Set the header for the chart to the latest month's total savings
                            (monthlyData.lastOrNull()?.get("savings") as? Double)?.let {
                                overallSavingsForChartHeader = it
                            }
                        } else {
                            savingsGrowthData = emptyList()
                            overallSavingsForChartHeader = 0.0
                        }
                        isLoadingSavingsGrowth = false
                    }

                    override fun onError(errorMessage: String) {
                        Log.e(TAG_ANALYTICS_SCREEN, "Error (SavingsGrowth): $errorMessage")
                        savingsGrowthData = emptyList()
                        savingsGrowthErrorMessage = "Error loading growth data: $errorMessage"
                        isLoadingSavingsGrowth = false
                        overallSavingsForChartHeader = 0.0
                    }
                }
            )
        } else {
            Log.e(
                TAG_ANALYTICS_SCREEN,
                "Invalid month/year for savings growth: $selectedMonth, $selectedYear"
            )
            savingsGrowthErrorMessage = "Invalid date for chart"
            isLoadingSavingsGrowth = false
            savingsGrowthData = emptyList()
            overallSavingsForChartHeader = 0.0
        }
    }


    val totalSavingsFormatted = if (isLoadingTotalSavings) "Loading..." // ...
    else if (totalSavingsErrorMessage != null) "Error"
    else String.format(Locale.getDefault(), "%.2f EUR", totalSavingsValue)

    val earnedFromInterestFormatted = if (isLoadingEarnedFromInterest) "Loading..." // ...
    else if (earnedFromInterestErrorMessage != null) "Error"
    else String.format(
        Locale.getDefault(),
        "%.2f %s",
        earnedFromInterestValue,
        earnedFromInterestCurrency
    )

    // --- NEW Formatter for "Earned this month" (Income Savings) ---
    val earnedThisMonthFormatted = if (isLoadingEarnedThisMonth) "Loading..."
    else if (earnedThisMonthErrorMessage != null) "Error"
    else String.format(
        Locale.getDefault(),
        "%.2f %s",
        earnedThisMonthValue,
        earnedThisMonthCurrency
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colors.background)
            .padding(24.dp)
    ) {
        // ... (Title, Back button, Download button, MonthYearSelector - remain the same) ...
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "ANALYTICS",
                fontSize = 20.sp,
                color = blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )

            IconButton(onClick = {
                downloadScreenAsPdf(context, view)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_download_24),
                    contentDescription = "Download"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                contentDescription = "Calendar",
                tint = blue,
                modifier = Modifier
                    .size(height = 40.dp, width = 40.dp)
                    .padding(end = 8.dp)
            )
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = {
                    selectedMonth = it /* Potentially re-trigger calculations if needed */
                },
                onYearSelected = {
                    selectedYear = it /* Potentially re-trigger calculations if needed */
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (totalSavingsErrorMessage != null && !isLoadingTotalSavings) { /* ... */
        }
        if (earnedFromInterestErrorMessage != null && !isLoadingEarnedFromInterest) { /* ... */
        }
        if (earnedThisMonthErrorMessage != null && !isLoadingEarnedThisMonth) {
            Text(
                text = "Monthly Earnings Error: ${earnedThisMonthErrorMessage!!}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (savingsGrowthErrorMessage != null && !isLoadingSavingsGrowth) {
            Text(
                text = "Chart Error: ${savingsGrowthErrorMessage!!}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard(
                value = totalSavingsFormatted,
                label = "Total saved",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            AnalyticsCard(
                value = earnedFromInterestFormatted,
                label = "Earned from interest",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            AnalyticsCard(
                value = earnedThisMonthFormatted,
                label = "Earned this month",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Savings Growth", style = typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        ChartCard {
            when {
                isLoadingSavingsGrowth -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                savingsGrowthData.isNotEmpty() -> {
                    LineChart(
                        chartDataPoints = savingsGrowthData,
                        headerValue = overallSavingsForChartHeader,
                        // VVVVVV ADD THE MISSING ARGUMENTS VVVVVV
                        compoundInterestText = compoundInterestSentence,
                        isLoadingCompoundInterestText = isLoadingCompoundInterestSentence
                    )
                }
                savingsGrowthErrorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading chart data.", // Or use savingsGrowthErrorMessage
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> { // No error, but no data (savingsGrowthData is empty)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No savings data available for the selected period to display chart.",
                            textAlign = TextAlign.Center,
                            style = typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier
            .fillMaxHeight() // 👈 force it to match tallest sibling
            .background(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = value,
                style = typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                ),
                maxLines = 1
            )
            Text(
                text = label,
                style = typography.bodySmall.copy(
                    color = colors.onSurface.copy(alpha = 0.7f)
                ),
                maxLines = 2,
                softWrap = true
            )
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

    val buttonColor = blue

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Button(
                onClick = { isMonthExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(selectedMonth)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Select Month",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = isMonthExpanded,
                onDismissRequest = { isMonthExpanded = false },
                modifier = Modifier
                    .background(buttonColor)
                    .heightIn(max = 300.dp)
            ) {
                months.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month, color = Color.White) },
                        onClick = {
                            onMonthSelected(month)
                            isMonthExpanded = false
                        }
                    )
                }
            }
        }

        Box {
            Button(
                onClick = { isYearExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(selectedYear)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Select Year",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = isYearExpanded,
                onDismissRequest = { isYearExpanded = false },
                modifier = Modifier
                    .background(buttonColor)
                    .heightIn(max = 200.dp)
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year, color = Color.White) },
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

fun downloadScreenAsPdf(context: Context, view: View) {
    val bitmap = view.drawToBitmap()
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = document.startPage(pageInfo)

    val canvas: Canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)

    val fileName = "AnalyticsReport.pdf"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri).use { out ->
                    document.writeTo(out)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
            }
        } else {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }

            Toast.makeText(context, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
    } finally {
        document.close()
    }
}

@Composable
fun ChartCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun LineChart(
    chartDataPoints: List<ChartDataPoint>,
    headerValue: Double,
    compoundInterestText: String?, // Parameter for the sentence
    isLoadingCompoundInterestText: Boolean // Parameter for its loading state
) {
    // The `when` block in AnalyticsScreen now handles the primary empty/loading state for the chart area.
    // This composable can assume chartDataPoints might be empty if its parent decided to render it that way,
    // but the most common case is it will have data.

    val values = chartDataPoints.map { it.value }
    val minY = remember(values) { values.minOrNull()?.let { if (it < 0f) it else 0f } ?: 0f }
    val maxY = remember(values) { values.maxOrNull()?.let { maxVal -> if (maxVal > minY) maxVal * 1.1f else minY + 100f } ?: (minY + 100f) }
    val monthLabels = chartDataPoints.map { it.label }

    val lineColor = blue
    val fillGradient = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
    )

    Column {
        // --- Chart Header ---
        Text(
            String.format(Locale.getDefault(), "%.2f EUR", headerValue),
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Cumulative Savings", color = lineColor)
        Spacer(modifier = Modifier.height(12.dp))

        // --- Chart Drawing Area ---
        if (chartDataPoints.isEmpty()) {
            // This will only show if AnalyticsScreen's `when` block lets it through
            // (e.g. if savingsGrowthData became empty after initially being non-empty, which is unlikely with current setup)
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No data points for chart.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val numYLabels = 6
                    val yLabelRange = (maxY - minY).coerceAtLeast(1f)
                    for (i in (numYLabels - 1) downTo 0) {
                        val labelValue = minY + (i.toFloat() / (numYLabels - 1).toFloat().coerceAtLeast(1f)) * yLabelRange
                        Text(
                            String.format(Locale.getDefault(), "%.0f", labelValue),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                // Chart Canvas
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Handle case of single data point for drawing just a circle
                        if (chartDataPoints.size == 1) {
                            val x = size.width / 2f
                            val yFraction = ((values.first() - minY) / (maxY - minY).coerceAtLeast(1f)).coerceIn(0f, 1f)
                            val y = size.height * (1 - yFraction)
                            val point = Offset(x.coerceIn(0f, size.width), y.coerceIn(0f, size.height))
                            drawCircle(color = lineColor, radius = 8f, center = point)
                            drawCircle(color = Color.White, radius = 4f, center = point)
                            return@Canvas
                        }
                        // If less than 2 points (and not 1), don't draw line/fill
                        if (chartDataPoints.size < 2) return@Canvas


                        val effectiveMaxY = if (maxY == minY) maxY + 1f else maxY
                        val rangeY = (effectiveMaxY - minY).coerceAtLeast(1f)

                        val points = values.mapIndexed { index, value ->
                            val xFraction = if (values.size > 1) index.toFloat() / (values.size - 1).toFloat() else 0.5f
                            val x = xFraction * size.width
                            val yFraction = ((value - minY) / rangeY).coerceIn(0f,1f)
                            val y = size.height * (1 - yFraction)
                            Offset(x.coerceIn(0f, size.width), y.coerceIn(0f, size.height))
                        }

                        // Draw Fill Path
                        val fillPath = Path().apply {
                            moveTo(points.first().x, size.height)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, size.height)
                            close()
                        }
                        drawPath(fillPath, brush = fillGradient)

                        // Draw Stroke Path
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(strokePath, color = lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

                        // Draw Circles
                        points.forEach {
                            drawCircle(color = lineColor, radius = 8f, center = it)
                            drawCircle(color = Color.White, radius = 4f, center = it)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels
        if (monthLabels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = (32.dp + 8.dp)),
                horizontalArrangement = if (monthLabels.size > 1) Arrangement.SpaceBetween else Arrangement.Center
            ) {
                monthLabels.forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Display the Compound Interest Projection Sentence ---
        Box(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingCompoundInterestText) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (compoundInterestText != null) {
                Text(
                    text = compoundInterestText,
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Optional: else { Text("Projection not available.") }
        }
    }
}

