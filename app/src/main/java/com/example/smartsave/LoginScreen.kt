package com.example.smartsave

import android.app.Activity // For finishing activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // For CircularProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator // Import this
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

// Assuming DashboardActivity exists
// class DashboardActivity : Activity() { /* ... */ }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // For displaying errors on screen
    var isLoading by remember { mutableStateOf(false) } // To show loading indicator

    // Initialize FirebaseAuth instance once
    val auth by remember { mutableStateOf(FirebaseAuth.getInstance()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Changed to Top for better layout with error message
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ready to start\nsaving smarter?",
            style = typography.headlineLarge,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Log in with your myPOS credentials\nto activate SmartSave.",
            style = typography.bodyLarge.copy(lineHeight = 22.sp),
            textAlign = TextAlign.Center,
            color = colors.onBackground.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it.trim() // Trim whitespace
                errorMessage = null // Clear error when user types
            },
            label = { Text("Username or Email", color = MaterialTheme.colorScheme.onBackground) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F4F4),
                unfocusedContainerColor = Color(0xFFF4F4F4),
                disabledContainerColor = Color(0xFFF4F4F4),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent, // Keep as transparent or change if you want visual error on field
                errorLabelColor = MaterialTheme.colorScheme.error // Standard error color for label
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            isError = errorMessage != null && (email.isBlank() || password.isBlank()) // Optionally highlight field on error
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Password", color = MaterialTheme.colorScheme.onBackground) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F4F4),
                unfocusedContainerColor = Color(0xFFF4F4F4),
                disabledContainerColor = Color(0xFFF4F4F4),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null && password.isBlank() // Optionally highlight field on error
        )

        // Display error message if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(if (errorMessage != null) 8.dp else 32.dp)) // Adjust spacer based on error message

        Button(
            onClick = {
                // 1. Clear previous error messages
                errorMessage = null

                // 2. Client-side validation
                if (email.isBlank()) {
                    errorMessage = "Email cannot be empty."
                    return@Button
                }
                // You might want more sophisticated email validation here
                // if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //     errorMessage = "Please enter a valid email address."
                //     return@Button
                // }
                if (password.isBlank()) {
                    errorMessage = "Password cannot be empty."
                    return@Button
                }

                isLoading = true // Show loading indicator

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false // Hide loading indicator
                        if (task.isSuccessful) {
                            // Login success – move to Dashboard
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, DashboardActivity::class.java)
                            // Clear back stack and start new task for DashboardActivity
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            // Finish the current LoginActivity
                            (context as? Activity)?.finish()
                        } else {
                            // Login failed
                            val exceptionMessage = task.exception?.message ?: "An unknown error occurred."
                            errorMessage = "Login failed: $exceptionMessage"
                            // You could also show a Toast here if you prefer,
                            // in addition to the on-screen error message.
                            // Toast.makeText(context, "Login failed: $exceptionMessage", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            enabled = !isLoading // Disable button while loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // Or a contrasting color
                )
            } else {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Don’t have a myPOS account?",
                style = typography.bodyMedium,
                color = colors.onBackground
            )

            Text(
                text = "Signup on myPOS.com",
                style = typography.bodyMedium.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://merchant.mypos.com/en/enroll#/".toUri()
                        )
                        context.startActivity(intent)
                    }
            )
        }
    }
}