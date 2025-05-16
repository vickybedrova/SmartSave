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
import androidx.annotation.RequiresApi

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
            // Android 10+ (API 29+)
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
            // Android 9 and below (API < 29)
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

