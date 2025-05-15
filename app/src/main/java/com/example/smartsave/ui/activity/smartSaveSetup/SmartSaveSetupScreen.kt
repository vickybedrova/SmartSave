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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.smartsave.R
import com.example.smartsave.ui.navigation.Screen

@Composable
fun SmartSaveSetupScreen(navController: NavController) {
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
                        append(stringResource(R.string.customize_your))
                    }
                    append("\n")
                    withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.smart_save_by_mypos))
                    }
                },
                style = typography.headlineSmall.copy(lineHeight = 32.sp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.smart_save_description),
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
                    text = stringResource(R.string.percentage_to_save),
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
                append(stringResource(R.string.agree_to_terms_prefix))
                pushStringAnnotation("terms", "https://example.com/terms")
                withStyle(SpanStyle(color = colors.primary)) {
                    append(stringResource(R.string.terms_and_conditions))
                }
                pop()
                append(stringResource(R.string.and))
                pushStringAnnotation("privacy", "https://example.com/privacy")
                withStyle(SpanStyle(color = colors.primary)) {
                    append(stringResource(R.string.privacy_statement))
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
               // Toast.makeText(context, "Saved with ${percentage.toInt()}%", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = stringResource(R.string.setup_smartsave),
                style = typography.labelLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}