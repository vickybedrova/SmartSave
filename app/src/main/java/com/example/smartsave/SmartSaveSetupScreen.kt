package com.example.smartsave

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

@Composable
fun SmartSaveSetupScreen() {
    var percentage by remember { mutableStateOf(5f) } // initial value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Customize your\n")
                    }
                    withStyle(SpanStyle(color = Color(0xFF0076D1), fontWeight = FontWeight.Bold)) {
                        append("SmartSave by myPOS")
                    }
                },
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "The selected percentage of each transaction will be transferred to your SmartSave savings account. The savings + the bonus will be automatically transferred by the month’s end.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Percentage to Save (%)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(percentage.toInt().toString(), fontSize = 16.sp)
            }

            Slider(
                value = percentage,
                onValueChange = { percentage = it },
                valueRange = 1f..15f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1%")
                Text("15%")
            }

            Spacer(modifier = Modifier.height(24.dp))

            val annotatedText = buildAnnotatedString {
                append("By clicking “Setup SmartSave” you agree to our ")
                pushStringAnnotation("terms", "https://example.com/terms")
                withStyle(SpanStyle(color = Color(0xFF0076D1))) {
                    append("Terms and Conditions")
                }
                pop()
                append(" and ")
                pushStringAnnotation("privacy", "https://example.com/privacy")
                withStyle(SpanStyle(color = Color(0xFF0076D1))) {
                    append("Privacy Statement")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                        Toast.makeText(context, "Open: ${annotation.item}", Toast.LENGTH_SHORT).show()
                    }
                },
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                Toast.makeText(context, "Saved with ${percentage.toInt()}%", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0076D1)),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Save Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSmartSaveSetupScreen() {
    SmartSaveSetupScreen()
}
