package com.example.smartsave.ui.activity.login

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.smartsave.ui.activity.dashboard.DashboardActivity
import com.example.smartsave.ui.activity.smartSaveSetup.SmartSaveSetupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
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
                email = it.trim()
                errorMessage = null
            },
            label = { Text("Username or Email", color = colors.onBackground) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            colors = textFieldColors(),
            textStyle = typography.bodyLarge.copy(color = colors.onBackground),
            isError = errorMessage != null && email.isBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password", color = colors.onBackground) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            colors = textFieldColors(),
            textStyle = typography.bodyLarge.copy(color = colors.onBackground),
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null && password.isBlank()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = colors.error,
                style = typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(if (errorMessage != null) 8.dp else 32.dp))

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
                                checkUserProfileAndNavigate(
                                    user = firebaseUser,
                                    context = context,
                                    auth = auth,
                                    setErrorMessage = { msg -> errorMessage = msg },
                                    onNavigationAttempted = { success ->
                                        isLoading = false
                                        if (success) activity?.finish()
                                    }
                                )
                            } else {
                                isLoading = false
                                errorMessage = "Login succeeded but user data is unavailable. Please try again."
                            }
                        } else {
                            isLoading = false
                            errorMessage = "Login failed: ${task.exception?.message ?: "Unknown error."}"
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
                    color = colors.onPrimary
                )
            } else {
                Text(
                    text = "Log In",
                    style = typography.labelLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
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
)

private const val SMART_SAVE_PROFILE_NODE = "smartSaveProfile"

private fun checkUserProfileAndNavigate(
    user: FirebaseUser,
    context: android.content.Context,
    auth: FirebaseAuth,
    setErrorMessage: (String?) -> Unit,
    onNavigationAttempted: (Boolean) -> Unit
) {
    val userId = user.uid
    val database = FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    val profileRef = database.reference.child(SMART_SAVE_PROFILE_NODE).child(userId)

    profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val nextActivity = if (snapshot.exists()) DashboardActivity::class.java
            else SmartSaveSetupActivity::class.java

            val intent = Intent(context, nextActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            onNavigationAttempted(true)
        }

        override fun onCancelled(error: DatabaseError) {
            setErrorMessage("Error accessing profile: ${error.message}")
            onNavigationAttempted(false)
        }
    })
}
