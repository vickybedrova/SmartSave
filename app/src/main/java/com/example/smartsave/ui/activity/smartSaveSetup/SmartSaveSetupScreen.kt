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
import kotlin.math.roundToInt // Import for rounding

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

    var percentage by remember { mutableStateOf(5f) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var existingProfile by remember { mutableStateOf<SmartSaveProfile?>(null) }
    var initialProfileFetched by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = currentUser) {
        if (currentUser == null) { /* ... navigation to login ... */
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
                        val loadedPercentage = it.savingsPercentage.toFloat()
                        Log.d(TAG_SETUP_SCREEN, "Profile loaded. DB savingsPercentage: ${it.savingsPercentage}, converted toFloat: $loadedPercentage")
                        percentage = loadedPercentage // Update slider
                    }
                } else { /* ... no existing profile ... */
                    Log.d(TAG_SETUP_SCREEN, "No existing profile found for user $userId.")
                    existingProfile = null
                }
                isLoading = false
                initialProfileFetched = true
            }
            override fun onCancelled(error: DatabaseError) { /* ... error handling ... */
                Log.e(TAG_SETUP_SCREEN, "Error fetching profile: ${error.message}", error.toException())
                Toast.makeText(context, "Error fetching profile: ${error.message}", Toast.LENGTH_LONG).show()
                isLoading = false
                initialProfileFetched = true
            }
        }
        profileRef.addListenerForSingleValueEvent(listener)
    }

    if (isLoading && !initialProfileFetched) { /* ... Loading UI ... */
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text( /* ... Title ... */
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
                Text( /* ... Description ... */
                    text = stringResource(R.string.smart_save_description),
                    style = typography.bodyMedium.copy(color = colors.onBackground.copy(alpha = 0.75f)),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row( /* ... Percentage Label ... */
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.percentage_to_save),
                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    // --- MODIFIED DISPLAY TO SHOW FLOAT FOR DEBUG ---
                    Text(
                        // text = "${percentage.toInt()}%", // Original display
                        text = "${percentage.roundToInt()}% ", // Display rounded and raw float
                        style = typography.bodyLarge
                    )
                    // --- END MODIFIED DISPLAY ---
                }

                Slider(
                    value = percentage,
                    onValueChange = { newValue ->
                        Log.d(TAG_SETUP_SCREEN, "Slider onValueChange - raw newValue from slider: $newValue")
                        // Option 1: Direct assignment (current behavior)
                        percentage = newValue
                        // Option 2: Force rounding (if slider produces tiny decimals despite steps)
                        // percentage = newValue.roundToInt().toFloat()
                        Log.d(TAG_SETUP_SCREEN, "Slider onValueChange - 'percentage' state updated to: $percentage")
                    },
                    valueRange = 1f..15f,
                    steps = 13, // This should give 14 discrete values: 1, 2, ..., 15
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                )
                Row( /* ... Min/Max Labels ... */
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1%", style = typography.bodySmall)
                    Text("15%", style = typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(24.dp))
                val annotatedText = buildAnnotatedString { /* ... Terms ... */
                    append(stringResource(R.string.agree_to_terms_prefix))
                    append(" ")
                    pushStringAnnotation("terms", "https://example.com/terms")
                    withStyle(SpanStyle(color = colors.primary)) {
                        append(stringResource(R.string.terms_and_conditions))
                    }
                    pop()
                    append(" ")
                    append(stringResource(R.string.and_conjunction))
                    append(" ")
                    pushStringAnnotation("privacy", "https://example.com/privacy")
                    withStyle(SpanStyle(color = colors.primary)) {
                        append(stringResource(R.string.privacy_statement))
                    }
                    pop()
                    append(".")
                }
                ClickableText( /* ... Clickable Terms ... */
                    text = annotatedText,
                    onClick = { offset ->
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
                onClick = {
                    if (currentUser == null) { /* ... user null check ... */
                        Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    val userId = currentUser.uid
                    val profileRef = database.reference.child("smartSaveProfile").child(userId)

                    // --- ADD LOGGING BEFORE SAVING ---
                    val percentageToSaveFloat = percentage // Current state (Float)
                    val percentageToSaveInt = percentage.roundToInt() // What you intend to save as whole number
                    val percentageToSaveDouble = percentageToSaveInt.toDouble() // The value actually being saved as Double

                    Log.d(TAG_SETUP_SCREEN, "Save Clicked: Raw 'percentage' state (Float): $percentageToSaveFloat")
                    Log.d(TAG_SETUP_SCREEN, "Save Clicked: Rounded 'percentage' state (Int): $percentageToSaveInt")
                    Log.d(TAG_SETUP_SCREEN, "Save Clicked: Value being saved to Firebase (Double): $percentageToSaveDouble")
                    // --- END LOGGING ---

                    val newSavingsPercentage = percentageToSaveDouble // Use the rounded integer then converted to double

                    if (existingProfile != null) {
                        Log.d(TAG_SETUP_SCREEN, "Updating existing profile. Saving percentage: $newSavingsPercentage for user $userId")
                        val updates = HashMap<String, Any>()
                        updates["savingsPercentage"] = newSavingsPercentage // Use the potentially rounded value
                        profileRef.updateChildren(updates)
                            .addOnSuccessListener { /* ... success navigation ... */
                                Log.d(TAG_SETUP_SCREEN, "SmartSave percentage updated successfully to $newSavingsPercentage")
                                Toast.makeText(context, "Savings percentage updated!", Toast.LENGTH_SHORT).show()
                                isSaving = false
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { /* ... failure ... */
                                Log.e(TAG_SETUP_SCREEN, "Failed to update SmartSave percentage for user $userId", it)
                                Toast.makeText(context, "Failed to update settings: ${it.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                            }
                    } else {
                        // --- CREATE MODE: Set the initial profile (as before) ---
                        Log.d(TAG_SETUP_SCREEN, "Creating new profile. Percentage: $newSavingsPercentage for user $userId")
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val currentDate = sdf.format(Date())
                        val newStartDate = currentDate
                        val newTotalSaved = 0.0 // Initial total saved

                        val profileToSave = SmartSaveProfile(
                            newSavingsPercentage,
                            newStartDate,
                            newTotalSaved,
                            true // <<< ENSURE isActive is set, typically to true
                        )
                        profileRef.setValue(profileToSave)
                            .addOnSuccessListener { /* ... success navigation ... */
                                Log.d(TAG_SETUP_SCREEN, "SmartSave profile created successfully with percentage $newSavingsPercentage")
                                Toast.makeText(context, "SmartSave settings saved!", Toast.LENGTH_SHORT).show()
                                isSaving = false
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { /* ... failure ... */
                                Log.e(TAG_SETUP_SCREEN, "Failed to create SmartSave profile for user $userId", it)
                                Toast.makeText(context, "Failed to save settings: ${it.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                enabled = !isSaving && initialProfileFetched
            ) { /* ... Button Text ... */
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
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
    }
}