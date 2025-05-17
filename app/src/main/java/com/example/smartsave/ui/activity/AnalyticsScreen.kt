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
import androidx.compose.material.icons.Icons
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

@Composable
fun AnalyticsScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val view = LocalView.current

    var selectedMonth by remember { mutableStateOf("May") }
    var selectedYear by remember { mutableStateOf("2025") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colors.background)
            .padding(24.dp)
    ) {

        Text(
            text = "ANALYTICS",
            fontSize = 20.sp,
            color = blue,
            fontWeight = FontWeight.Bold,
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
            }

            IconButton(onClick = {
                downloadScreenAsPdf(context, view)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_download_24),
                    contentDescription = "Download"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    .padding(end = 8.dp)            )
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = { selectedMonth = it },
                onYearSelected = { selectedYear = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnalyticsCard(value = "750 BGN", label = "Total Saved")
            AnalyticsCard(value = "5 BGN", label = "This Month")
            AnalyticsCard(value = "3", label = "Progress This Month")
        }


//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text("Monthly Breakdown", style = typography.titleMedium)
//        Spacer(modifier = Modifier.height(8.dp))

//        ChartCard {
//            BarChart()
//        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Savings Growth", style = typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        ChartCard {
            LineChart()
        }
    }
}

@Composable
fun AnalyticsCard(value: String, label: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 120.dp)
            .background(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = value,
                style = typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            )
            Text(
                text = label,
                style = typography.bodySmall.copy(
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
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
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month", tint = Color.White)
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
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Year", tint = Color.White)
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
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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
fun LineChart() {
    val values = listOf(2_000f, 4_000f, 6_000f, 5_000f, 8_000f, 9_000f, 7_500f, 10_000f, 8_500f)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")

    val maxY = 10_000f
    val minY = 0f

    val lineColor = MaterialTheme.colorScheme.primary
    val fillGradient = Brush.verticalGradient(
        colors = listOf(
            lineColor.copy(alpha = 0.3f),
            Color.Transparent
        )
    )

    Column {
        Text("23’261 BGN", style = MaterialTheme.typography.headlineSmall)
        Text("For current year", color = lineColor)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 5 downTo 0) {
                    Text("${i * 2000}", style = MaterialTheme.typography.labelSmall)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    val spacing = size.width / (values.size - 1)
                    val height = size.height

                    val points = values.mapIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value - minY) / (maxY - minY) * height
                        Offset(x, y)
                    }

                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        for (i in points.indices) {
                            val point = points[i]
                            if (i == 0) lineTo(point.x, point.y)
                            else {
                                val prev = points[i - 1]
                                val mid = Offset((prev.x + point.x) / 2, (prev.y + point.y) / 2)
                                quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
                            }
                        }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(fillPath, brush = fillGradient)

                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val current = points[i]
                            val mid = Offset((prev.x + current.x) / 2, (prev.y + current.y) / 2)
                            quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
                        }
                    }
                    drawPath(strokePath, color = lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

                    points.forEach {
                        drawCircle(color = lineColor, radius = 6f, center = it)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}




//@Composable
//fun BarChart() {
//    val barValues = listOf(8000f, 6000f, 4000f, 7000f, 5000f, 6000f, 5000f, 9000f, 3000f)
//    val interestValues = listOf(1500f, 1000f, 500f, 1200f, 800f, 900f, 700f, 1300f, 400f)
//    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")
//    val maxDisplayValue = 10000f // For scaling
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("48’079 BGN", style = MaterialTheme.typography.headlineSmall)
//        Text("Profits", color = MaterialTheme.colorScheme.primary)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(180.dp)
//        ) {
//            // Y-axis labels with even vertical spacing (aligned with bar height only)
//            Column(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .padding(bottom = 24.dp, end = 8.dp), // reserve space for months
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                listOf(10000, 8000, 6000, 4000, 2000, 0).forEach { label ->
//                    Text(
//                        "$label",
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.align(Alignment.End)
//                    )
//                }
//            }
//
//
//            // Bars and X-axis labels container
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Bottom,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Row(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    barValues.forEachIndexed { index, value ->
//                        val interest = interestValues.getOrNull(index) ?: 0f
//                        val baseValue = (value - interest).coerceAtLeast(0f)
//
//                        val baseHeightRatio = baseValue / maxDisplayValue
//                        val interestHeightRatio = interest / maxDisplayValue
//
//                        val baseHeight = (baseHeightRatio * 140).dp
//                        val interestHeight = (interestHeightRatio * 140).dp
//
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Bottom
//                        ) {
//                            // Base part of the bar
//                            Box(
//                                modifier = Modifier
//                                    .width(16.dp)
//                                    .height(baseHeight)
//                                    .background(
//                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
//                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
//                                    )
//                            )
//                            // Interest part stacked on top
//                            Box(
//                                modifier = Modifier
//                                    .width(16.dp)
//                                    .height(interestHeight)
//                                    .background(
//                                        color = MaterialTheme.colorScheme.secondary,
//                                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
//                                    )
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    months.forEach { month ->
//                        Text(
//                            month,
//                            style = MaterialTheme.typography.labelSmall,
//                            modifier = Modifier.width(24.dp),
//                        )
//                    }
//                }
//            }
//        }
//    }
//}







