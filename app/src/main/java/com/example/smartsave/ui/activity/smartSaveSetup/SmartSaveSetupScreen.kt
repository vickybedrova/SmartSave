package com.example.smartsave.ui.activity.smartSaveSetup

import android.util.Log
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
import com.example.smartsave.model.SmartSaveProfile // Import your data class
import com.example.smartsave.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG_SETUP_SCREEN = "SmartSaveSetupScreen"

@Composable
fun SmartSaveSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember {
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    }
    val currentUser = auth.currentUser

    // State for the UI
    var percentage by remember { mutableStateOf(5f) } // Slider value (1f to 15f)
    var isLoading by remember { mutableStateOf(true) } // For fetching existing data
    var isSaving by remember { mutableStateOf(false) } // For save operation

    // State to hold existing profile data if found
    var existingProfile by remember { mutableStateOf<SmartSaveProfile?>(null) }
    var initialProfileFetched by remember { mutableStateOf(false) }

    // Fetch existing profile when the screen loads for the current user
    LaunchedEffect(key1 = currentUser) {
        if (currentUser == null) {
            Log.w(TAG_SETUP_SCREEN, "No user, navigating to login.")
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            return@LaunchedEffect
        }

        isLoading = true
        val userId = currentUser.uid
        val profileRef = database.reference.child("smartSaveProfile").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profile = snapshot.getValue(SmartSaveProfile::class.java)
                    existingProfile = profile
                    profile?.let {
                        percentage = it.savingsPercentage.toFloat() // Update slider
                        Log.d(TAG_SETUP_SCREEN, "Existing profile loaded: $it")
                    }
                } else {
                    Log.d(TAG_SETUP_SCREEN, "No existing profile found for user $userId.")
                    existingProfile = null // Ensure it's null if no profile
                }
                isLoading = false
                initialProfileFetched = true
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG_SETUP_SCREEN, "Error fetching profile: ${error.message}", error.toException())
                Toast.makeText(context, "Error fetching profile: ${error.message}", Toast.LENGTH_LONG).show()
                isLoading = false
                initialProfileFetched = true // Mark as fetched even on error to unblock UI
            }
        }
        profileRef.addListenerForSingleValueEvent(listener) // Use single event to not keep listening
    }


    if (isLoading && !initialProfileFetched) { // Show loading only on initial fetch
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ... (Your existing UI for title, description, slider, terms - no changes needed here) ...
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
                    steps = 13, // (15-1) = 14 segments, 13 steps
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
                    append(" ")
                    pushStringAnnotation("terms", "https://example.com/terms")
                    withStyle(SpanStyle(color = colors.primary)) {
                        append(stringResource(R.string.terms_and_conditions))
                    }
                    pop()
                    append(" ")
                    append(stringResource(R.string.and_conjunction)) // Assuming you renamed "and"
                    append(" ")
                    pushStringAnnotation("privacy", "https://example.com/privacy")
                    withStyle(SpanStyle(color = colors.primary)) {
                        append(stringResource(R.string.privacy_statement))
                    }
                    pop()
                    append(".")
                }

                ClickableText(
                    text = annotatedText,
                    onClick = { offset -> /* ... your existing click logic ... */
                        annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                Toast.makeText(context, "Open Terms: ${annotation.item}", Toast.LENGTH_SHORT).show()
                            }
                        annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                Toast.makeText(context, "Open Privacy: ${annotation.item}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    style = typography.bodySmall.copy(fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 16.sp),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                // In SmartSaveSetupScreen.kt, inside Button onClick:

                onClick = {
                    if (currentUser == null) {
                        Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    val userId = currentUser.uid
                    val profileRef = database.reference.child("smartSaveProfile").child(userId)
                    val newSavingsPercentage = percentage.toDouble()

                    if (existingProfile != null) {
                        // --- EDIT MODE: Update only the savingsPercentage ---
                        Log.d(TAG_SETUP_SCREEN, "Updating existing profile. New percentage: $newSavingsPercentage for user $userId")
                        val updates = HashMap<String, Any>()
                        updates["savingsPercentage"] = newSavingsPercentage
                        // We don't update startDate or totalSaved here, they are preserved.
                        // The 'transactions' node is also not touched by updateChildren at this path.

                        profileRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Log.d(TAG_SETUP_SCREEN, "SmartSave percentage updated successfully for user $userId")
                                Toast.makeText(context, "Savings percentage updated!", Toast.LENGTH_SHORT).show()
                                isSaving = false

                                // --- Ensure Dashboard refreshes ---
                                // Option 1: Just navigate. Dashboard's listeners should pick up the change.
                                navController.navigate(Screen.Dashboard.route) {
                                    // If coming from Dashboard (edit mode), pop only Setup
                                    // If first time setup, pop up to Login
                                    // This needs a way to distinguish edit vs create for navigation.
                                    // For now, let's assume a consistent pop for simplicity,
                                    // or handle this with a passed argument to SmartSaveSetupScreen.
                                    popUpTo(Screen.Login.route) { inclusive = true } // Or better, popUpTo(Screen.Setup.route){ inclusive = true } if truly an edit
                                }

                                // Option 2 (More proactive, if needed, but usually not):
                                // You could pass a lambda from Dashboard to Setup to trigger a manual refresh,
                                // or use a shared ViewModel. But Firebase listeners usually suffice.
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG_SETUP_SCREEN, "Failed to update SmartSave percentage for user $userId", e)
                                Toast.makeText(context, "Failed to update settings: ${e.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                            }
                    } else {
                        // --- CREATE MODE: Set the initial profile (as before) ---
                        Log.d(TAG_SETUP_SCREEN, "Creating new profile. Percentage: $newSavingsPercentage for user $userId")
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val currentDate = sdf.format(Date())
                        val newStartDate = currentDate
                        val newTotalSaved = 0.0 // Initial total saved

                        // Using your Java class constructor for SmartSaveProfile
                        val profileToSave = SmartSaveProfile(
                            newSavingsPercentage,
                            newStartDate,
                            newTotalSaved
                        )
                        // This will create the profile node with only these fields.
                        // The 'transactions' node will be added later under this profileRef.
                        profileRef.setValue(profileToSave)
                            .addOnSuccessListener {
                                Log.d(TAG_SETUP_SCREEN, "SmartSave profile created successfully for user $userId")
                                Toast.makeText(context, "SmartSave settings saved!", Toast.LENGTH_SHORT).show()
                                isSaving = false
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG_SETUP_SCREEN, "Failed to create SmartSave profile for user $userId", e)
                                Toast.makeText(context, "Failed to save settings: ${e.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                            }
                    }
                },
// ... (rest of Button parameters)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                enabled = !isSaving && initialProfileFetched // Enable button only if not saving and initial data load attempt is done
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = stringResource(R.string.setup_smartsave), // Or "Save Changes" if existingProfile != null
                        style = typography.labelLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}