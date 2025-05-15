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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseUser // Already have FirebaseAuth
import android.util.Log

// Assuming DashboardActivity exists
// class DashboardActivity : Activity() { /* ... */ }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
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
                errorMessage = null
                if (email.isBlank()) {
                    errorMessage = "Email cannot be empty."
                    return@Button
                }
                if (password.isBlank()) {
                    errorMessage = "Password cannot be empty."
                    return@Button
                }

                isLoading = true

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                Log.i(TAG_LOGIN_LOGIC, "Login successful for: ${firebaseUser.email}")
                                // Call the profile check function
                                checkUserProfileAndNavigate(
                                    user = firebaseUser,
                                    context = context,
                                    auth = auth,
                                    setErrorMessage = { msg -> errorMessage = msg },
                                    onNavigationAttempted = { success ->
                                        isLoading = false // Hide loading regardless of DB check outcome
                                        if (success) {
                                            // Navigation was initiated, finish LoginActivity
                                            activity?.finish()
                                        }
                                        // If !success, errorMessage is already set by checkUserProfileAndNavigate
                                    }
                                )
                            } else {
                                // This case should be rare if task.isSuccessful is true
                                isLoading = false
                                errorMessage = "Login succeeded but user data is unavailable. Please try again."
                                Log.e(TAG_LOGIN_LOGIC, "Login task successful but currentUser is null.")
                            }
                        } else {
                            isLoading = false
                            val exceptionMessage = task.exception?.message ?: "An unknown error occurred."
                            errorMessage = "Login failed: $exceptionMessage"
                            Log.w(TAG_LOGIN_LOGIC, "Login failed: $exceptionMessage", task.exception)
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
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

private const val TAG_LOGIN_LOGIC = "LoginLogic" // For Logcat
private const val SMART_SAVE_PROFILE_NODE = "smartSaveProfile" // Your database node name

private fun checkUserProfileAndNavigate(
    user: FirebaseUser,
    context: android.content.Context,
    auth: FirebaseAuth, // Pass for potential sign-out on critical DB error
    setErrorMessage: (String?) -> Unit, // To update UI error message
    onNavigationAttempted: (success: Boolean) -> Unit // Callback to signal completion
) {
    val userId = user.uid
    // IMPORTANT: Specify your database URL
    val database = FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    val profileRef = database.reference.child(SMART_SAVE_PROFILE_NODE).child(userId)

    Log.d(TAG_LOGIN_LOGIC, "Checking profile for user: $userId at path: ${profileRef.toString()}")

    profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val targetActivityClass: Class<out Activity>
            if (dataSnapshot.exists()) {
                Log.i(TAG_LOGIN_LOGIC, "SmartSave Profile found for user: $userId. Navigating to Dashboard.")
                // Optionally retrieve data if needed immediately:
                // val profileData = dataSnapshot.getValue(SmartSaveProfileData::class.java) // Assuming you have a data class
                targetActivityClass = DashboardActivity::class.java
            } else {
                Log.i(TAG_LOGIN_LOGIC, "No SmartSave Profile for user: $userId. Navigating to Setup.")
                targetActivityClass = SmartSaveSetupActivity::class.java
            }

            val intent = Intent(context, targetActivityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            // Finish the LoginActivity from the calling composable's context after this callback
            onNavigationAttempted(true)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(TAG_LOGIN_LOGIC, "Failed to read profile for user: $userId", databaseError.toException())
            // Decide how to handle this critical error.
            // Option 1: Show error and let user retry login (which re-triggers this check)
            setErrorMessage("Error accessing your profile: ${databaseError.message}. Please try logging in again.")
            // Option 2: Sign out user if profile check is absolutely mandatory for app function.
            // auth.signOut()
            // setErrorMessage("Critical error accessing your profile. You have been logged out. Please try again.")
            onNavigationAttempted(false) // Indicate failure
        }
    })
}