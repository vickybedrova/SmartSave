package com.example.smartsave.ui.activity.login

import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.example.smartsave.data.RetrofitClient // Import RetrofitClient
import com.example.smartsave.data.TokenManager   // Import TokenManager
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.ui.theme.SmartSaveTextFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG_LOGIN_SCREEN = "LoginScreenLogicMyPOS"

// Your merchant client credentials
private const val MYPOS_CLIENT_ID = "cli_sbx2xNXBcq19lAHdA8z2ZnOPjrxY"
private const val MYPOS_CLIENT_SECRET = "sec_Fg4yPdCVt0zsFfd8NFtIn4C9zErrF4Sed5OaCq0OnVHZmjWazprZdRfUZarD"

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Check if already logged in (tokens exist)
    LaunchedEffect(Unit) {
        if (TokenManager.hasTokens(context)) {
            Log.i(TAG_LOGIN_SCREEN, "Tokens found, navigating to Dashboard.")
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (UI TextFields, etc. - remain the same)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Ready to start\nsaving smarter?", style = typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Log in with your myPOS credentials\nto activate SmartSave.", style = typography.bodyLarge.copy(lineHeight = 22.sp), textAlign = TextAlign.Center, color = colors.onBackground.copy(alpha = 0.85f))
        Spacer(modifier = Modifier.height(32.dp))

        TextField(value = email, onValueChange = { email = it.trim(); errorMessage = null }, label = { Text("Username or Email") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = SmartSaveTextFieldColors(), textStyle = typography.bodyLarge, isError = errorMessage != null && email.isBlank())
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = SmartSaveTextFieldColors(), textStyle = typography.bodyLarge, isError = errorMessage != null && password.isBlank())

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = colors.error, style = typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
                Log.d(TAG_LOGIN_SCREEN, "Login button clicked. Attempting myPOS OAuth for: $email")

                coroutineScope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.getOAuthToken(
                                clientId = MYPOS_CLIENT_ID,
                                clientSecret = MYPOS_CLIENT_SECRET,
                                username = email,
                                password = password
                            ).execute()
                        }

                        if (response.isSuccessful) {
                            val tokenResponse = response.body()
                            if (tokenResponse != null) {
                                Log.i(TAG_LOGIN_SCREEN, "myPOS OAuth successful. Session: ${tokenResponse.sessionId}, AccessToken: ${tokenResponse.accessToken}")
                                TokenManager.saveTokens(
                                    context,
                                    tokenResponse.sessionId,
                                    tokenResponse.accessToken,
                                    tokenResponse.tokenType
                                )
                                // Navigate to Dashboard
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                errorMessage = "Login failed: Empty response from server."
                                Log.e(TAG_LOGIN_SCREEN, "myPOS OAuth successful but response body is null.")
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            errorMessage = "Login failed: ${response.code()} - $errorBody"
                            Log.e(TAG_LOGIN_SCREEN, "myPOS OAuth failed: ${response.code()} - $errorBody")
                        }
                    } catch (e: IOException) {
                        errorMessage = "Network error. Please check your connection."
                        Log.e(TAG_LOGIN_SCREEN, "Network error during OAuth: ${e.message}", e)
                    } catch (e: Exception) {
                        errorMessage = "An unexpected error occurred during login."
                        Log.e(TAG_LOGIN_SCREEN, "Unexpected error during OAuth: ${e.message}", e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
            } else {
                Text(text = "Log In", style = typography.labelLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }
        }

        // ... (Signup link - remains the same) ...
        Spacer(modifier = Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Don’t have a myPOS account?", style = typography.bodyMedium)
            Text(text = "Signup on myPOS.com", style = typography.bodyMedium.copy(color = colors.primary, fontWeight = FontWeight.Medium), modifier = Modifier.padding(top = 4.dp).clickable {
                val uri = "https://merchant.mypos.com/en/enroll#/".toUri(); context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            })
        }
    }
}