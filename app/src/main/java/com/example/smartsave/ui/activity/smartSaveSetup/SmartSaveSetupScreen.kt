package com.example.smartsave.ui.activity.smartSaveSetup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var percentage by remember { mutableStateOf(5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Customize your\n")
                    }
                    withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                        append("SmartSave by myPOS")
                    }
                },
                style = typography.headlineSmall.copy(lineHeight = 32.sp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "The selected percentage of each transaction will be transferred to your SmartSave savings account. The savings + the bonus will be automatically transferred by the month’s end.",
                style = typography.bodyMedium.copy(color = colors.onBackground.copy(alpha = 0.75f)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Percentage to Save (%)",
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "${percentage.toInt()}%",
                    style = typography.bodyLarge
                )
            }

            Slider(
                value = percentage,
                onValueChange = { percentage = it },
                valueRange = 1f..15f,
                steps = 13,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1%", style = typography.bodySmall)
                Text("15%", style = typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val annotatedText = buildAnnotatedString {
                append("By clicking “Setup SmartSave” you agree to our ")
                pushStringAnnotation("terms", "https://example.com/terms")
                withStyle(SpanStyle(color = colors.primary)) {
                    append("Terms and Conditions")
                }
                pop()
                append(" and ")
                pushStringAnnotation("privacy", "https://example.com/privacy")
                withStyle(SpanStyle(color = colors.primary)) {
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
                style = typography.bodySmall.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
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
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "Save Setup",
                style = typography.labelLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSmartSaveSetupScreen() {
    SmartSaveSetupScreen()
}