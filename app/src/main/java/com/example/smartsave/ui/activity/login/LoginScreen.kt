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
import com.example.smartsave.data.RetrofitClient
import com.example.smartsave.data.TokenManager
import com.example.smartsave.data.models.AuthSessionRequest // Import for the request body
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.ui.theme.SmartSaveTextFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG_LOGIN_SCREEN = "LoginScreenLogicMyPOS"

// Your merchant client credentials
private const val MYPOS_CLIENT_ID = "MsUD21mngVnEMhuiB0whgnyq"
private const val MYPOS_CLIENT_SECRET = "Ayb3a02pkiil67e8usPln0fKOpHJzBi5cDMIgjObd40J4WLG"

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // UI state for email and password.
    // Note: These are NOT directly used for the current myPOS API calls,
    // which use the hardcoded MYPOS_CLIENT_ID and MYPOS_CLIENT_SECRET.
    // You might want to clarify their purpose or remove them if this screen
    // is solely for the non-interactive myPOS login.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

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
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Ready to start\nsaving smarter?", style = typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Log in with your myPOS credentials\nto activate SmartSave.", style = typography.bodyLarge.copy(lineHeight = 22.sp), textAlign = TextAlign.Center, color = colors.onBackground.copy(alpha = 0.85f))
        Spacer(modifier = Modifier.height(32.dp))

        // Email and Password fields - kept for UI consistency, but not used in current API calls
        TextField(value = email, onValueChange = { email = it.trim(); errorMessage = null }, label = { Text("Username or Email (Optional)") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = SmartSaveTextFieldColors(), textStyle = typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text("Password (Optional)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = SmartSaveTextFieldColors(), textStyle = typography.bodyLarge)

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = colors.error, style = typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(if (errorMessage != null) 8.dp else 32.dp))

        Button(
            onClick = {
                errorMessage = null
                // Optional: You can keep local validation for these fields if they serve another purpose.
                // if (email.isBlank() || password.isBlank()) {
                //     errorMessage = "Please fill in all fields."
                //     return@Button
                // }

                isLoading = true
                Log.d(TAG_LOGIN_SCREEN, "Login button clicked. Attempting myPOS Non-Interactive User Login Flow.")

                coroutineScope.launch {
                    try {
                        // Step 1: Get OAuth Token (Client Credentials)
                        Log.d(TAG_LOGIN_SCREEN, "Step 1: Requesting OAuth token...")
                        val oauthResponse = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.getClientCredentialsToken(
                                clientId = MYPOS_CLIENT_ID,
                                clientSecret = MYPOS_CLIENT_SECRET
                                // grant_type defaults to "client_credentials" in the ApiService
                            )
                        }

                        if (oauthResponse.isSuccessful) {
                            val oauthTokenData = oauthResponse.body()
                            if (oauthTokenData?.accessToken != null && oauthTokenData.tokenType != null) {
                                Log.i(TAG_LOGIN_SCREEN, "Step 1: OAuth token received: ${oauthTokenData.accessToken}")

                                // Step 2: Get Auth Session
                                val authorizationHeader = "${oauthTokenData.tokenType} ${oauthTokenData.accessToken}"
                                val authSessionRequest = AuthSessionRequest(
                                    clientId = MYPOS_CLIENT_ID,
                                    clientSecret = MYPOS_CLIENT_SECRET
                                )

                                Log.d(TAG_LOGIN_SCREEN, "Step 2: Requesting Auth Session...")
                                val sessionApiResponse = withContext(Dispatchers.IO) {
                                    RetrofitClient.apiService.getAuthSession(
                                        authorization = authorizationHeader,
                                        body = authSessionRequest
                                    )
                                }

                                if (sessionApiResponse.isSuccessful) {
                                    val sessionData = sessionApiResponse.body()
                                    if (sessionData?.session != null) {
                                        Log.i(TAG_LOGIN_SCREEN, "Step 2: Auth Session successful. Session ID: ${sessionData.session}")
                                        TokenManager.saveTokens(
                                            context,
                                            sessionData.session,         // Session ID from Step 2
                                            oauthTokenData.accessToken,  // Access Token from Step 1
                                            oauthTokenData.tokenType     // Token Type from Step 1
                                        )
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = "Login failed: Could not retrieve session ID."
                                        Log.e(TAG_LOGIN_SCREEN, "Step 2: Auth Session response body or session ID is null. Body: $sessionData")
                                    }
                                } else {
                                    val errorBody = sessionApiResponse.errorBody()?.string() ?: "Unknown error"
                                    errorMessage = "Login failed (Session): ${sessionApiResponse.code()} - $errorBody"
                                    Log.e(TAG_LOGIN_SCREEN, "Step 2: Auth Session failed: ${sessionApiResponse.code()} - $errorBody")
                                }
                            } else {
                                errorMessage = "Login failed: Invalid OAuth token response."
                                Log.e(TAG_LOGIN_SCREEN, "Step 1: OAuth token response body, access token, or token type is null. Body: $oauthTokenData")
                            }
                        } else {
                            val errorBody = oauthResponse.errorBody()?.string() ?: "Unknown error"
                            errorMessage = "Login failed (OAuth): ${oauthResponse.code()} - $errorBody"
                            Log.e(TAG_LOGIN_SCREEN, "Step 1: OAuth token request failed: ${oauthResponse.code()} - $errorBody")
                        }
                    } catch (e: IOException) {
                        errorMessage = "Network error. Please check your connection."
                        Log.e(TAG_LOGIN_SCREEN, "Network error during login: ${e.message}", e)
                    } catch (e: Exception) {
                        errorMessage = "An unexpected error occurred during login."
                        Log.e(TAG_LOGIN_SCREEN, "Unexpected error during login: ${e.message}", e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
            } else {
                Text(text = "Log In", style = typography.labelLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Don’t have a myPOS account?", style = typography.bodyMedium)
            Text(
                text = "Signup on myPOS.com",
                style = typography.bodyMedium.copy(color = colors.primary, fontWeight = FontWeight.Medium),
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