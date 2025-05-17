package com.example.smartsave.ui.activity.login

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.ui.theme.SmartSaveTextFieldColors
import com.example.smartsave.ui.theme.blue
import com.example.smartsave.util.SavingsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ready to start\nsaving smarter?",
            style = typography.headlineLarge,
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
            label = { Text("Username or Email") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = SmartSaveTextFieldColors(),
            textStyle = typography.bodyLarge,
            isError = errorMessage != null && email.isBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = SmartSaveTextFieldColors(),
            textStyle = typography.bodyLarge,
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
                            val user = auth.currentUser
                            if (user != null) {
                                checkUserProfileAndNavigate(
                                    user = user,
                                    navController = navController,
                                    setErrorMessage = { msg ->
                                        errorMessage = msg
                                    },
                                    onNavigationAttempted = { navAttempted ->
                                        isLoading = false
                                    }
                                )
                            } else {
                                isLoading = false
                                errorMessage = "Login succeeded but user is null."
                            }
                        } else {
                            isLoading = false
                            val exceptionMessage = task.exception?.message ?: "Unknown error."
                            errorMessage = "Login failed: $exceptionMessage"
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = blue,
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.onPrimary
                )
            } else {
                Text(
                    text = "Log In",
                    style = typography.labelLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Don’t have a myPOS account?", style = typography.bodyMedium)
            Text(
                text = "Signup on myPOS.com",
                style = typography.bodyMedium.copy(
                    color = blue,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        val uri = "https://merchant.mypos.com/en/enroll#/".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
            )
        }
    }
}

private const val TAG_LOGIN_SCREEN = "LoginScreenLogic"
private const val SMART_SAVE_PROFILE_NODE = "smartSaveProfile"

private fun checkUserProfileAndNavigate(
    user: FirebaseUser,
    navController: NavController,
    setErrorMessage: (String?) -> Unit,
    onNavigationAttempted: (Boolean) -> Unit
) {
    val userId = user.uid
    val db =
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    val profileRef = db.reference.child(SMART_SAVE_PROFILE_NODE).child(userId)

    profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val route: String
            if (dataSnapshot.exists()) {
                route = Screen.Dashboard.route
                SavingsCalculator.recalculateAndUpdatetotalSaved(object :
                    SavingsCalculator.CalculationCallback {
                    override fun onSuccess(newTotalSaved: Double) {
                    }

                    override fun onError(errorMessage: String) {
                    }
                })

            } else {
                route = Screen.Setup.route
            }

            try {
                val currentDestination = navController.currentDestination?.route
                if (currentDestination == Screen.Login.route) {
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    onNavigationAttempted(true)
                } else {
                    onNavigationAttempted(false)
                }
            } catch (e: Exception) {
                setErrorMessage("Navigation error. Please try again.")
                onNavigationAttempted(false)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            setErrorMessage("Error accessing profile: ${databaseError.message}")
            onNavigationAttempted(false)
        }
    })
}