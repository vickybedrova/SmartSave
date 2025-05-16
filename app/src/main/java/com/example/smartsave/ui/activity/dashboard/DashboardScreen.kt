package com.example.smartsave.ui.activity.dashboard

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.smartsave.DashboardContent
import com.example.smartsave.model.Transaction
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.util.SavingsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser // Import FirebaseUser
import com.google.firebase.database.*
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val TAG_DASHBOARD_SCREEN = "DashboardScreenLogic"

@Composable
fun DashboardScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember {
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    }

    // --- State to hold the user, updated by AuthStateListener ---
    var currentAuthUser by remember { mutableStateOf(auth.currentUser) }

    // States for data
    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var totalSavings by remember { mutableStateOf(0.0) }
    var savingsPercentage by remember { mutableStateOf(0.0) }
    var earnedThisMonth by remember { mutableStateOf(0.0) }
    var earnedThisMonthCurrency by remember { mutableStateOf("EUR") }
    var progressThisMonth by remember { mutableStateOf(0.0) }
    var progressThisMonthCurrency by remember { mutableStateOf("EUR") }

    // Granular Loading States
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isLoadingTransactions by remember { mutableStateOf(true) }
    var isLoadingEarnedThisMonth by remember { mutableStateOf(true) }
    var isLoadingProgressThisMonth by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoadingOverall = isLoadingProfile || isLoadingTransactions || isLoadingEarnedThisMonth || isLoadingProgressThisMonth

    // --- Use DisposableEffect to manage AuthStateListener ---
    DisposableEffect(key1 = auth) { // Key to auth instance
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.d(TAG_DASHBOARD_SCREEN, "AuthStateListener: User changed to: ${user?.uid}")
            if (currentAuthUser?.uid != user?.uid) { // Only update state if it actually changed
                currentAuthUser = user
            }
        }
        auth.addAuthStateListener(authStateListener)
        Log.d(TAG_DASHBOARD_SCREEN, "AuthStateListener ADDED.")
        onDispose {
            auth.removeAuthStateListener(authStateListener)
            Log.d(TAG_DASHBOARD_SCREEN, "AuthStateListener REMOVED.")
        }
    }

    // This LaunchedEffect now reacts to changes in `currentAuthUser`
    LaunchedEffect(key1 = currentAuthUser) {
        Log.d(TAG_DASHBOARD_SCREEN, "LaunchedEffect triggered by currentAuthUser. UID: ${currentAuthUser?.uid}")
        if (currentAuthUser == null) {
            Log.w(TAG_DASHBOARD_SCREEN, "currentAuthUser is null, navigating to Login.")
            try {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                Log.i(TAG_DASHBOARD_SCREEN, "Navigation to Login screen initiated.")
            } catch (e: Exception) {
                Log.e(TAG_DASHBOARD_SCREEN, "Error navigating to Login: ${e.message}", e)
            }
        } else {
            // User is logged in, reset states and fetch data
            val userId = currentAuthUser!!.uid // Safe due to null check
            Log.d(TAG_DASHBOARD_SCREEN, "User $userId detected by LaunchedEffect. Resetting loading states.")
            isLoadingProfile = true
            isLoadingTransactions = true
            isLoadingEarnedThisMonth = true
            isLoadingProgressThisMonth = true
            errorMessage = null
            transactionsList = emptyList()
            totalSavings = 0.0
            savingsPercentage = 0.0
            earnedThisMonth = 0.0
            earnedThisMonthCurrency = "EUR"
            progressThisMonth = 0.0
            progressThisMonthCurrency = "EUR"

            // Data fetching will now be in separate DisposableEffects keyed to userId
        }
    }

    // Data fetching DisposableEffects - only run if currentAuthUser is not null
    currentAuthUser?.let { user -> // Execute only if user is not null
        val userId = user.uid
        val userProfileRef = database.reference.child("smartSaveProfile").child(userId)

        // DisposableEffect for Profile Data
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (Profile): Setting up for user $userId")
            isLoadingProfile = true
            val profileListener = userProfileRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { /* ... update states, isLoadingProfile = false ... */
                    Log.d(TAG_DASHBOARD_SCREEN, "Profile data received for $userId.")
                    totalSavings = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0
                    savingsPercentage = snapshot.child("savingsPercentage").getValue(Double::class.java) ?: 0.0
                    isLoadingProfile = false
                }
                override fun onCancelled(error: DatabaseError) { /* ... handle error, isLoadingProfile = false ... */
                    Log.e(TAG_DASHBOARD_SCREEN, "Profile data onCancelled for $userId: ${error.message}", error.toException())
                    errorMessage = (errorMessage ?: "") + "\nProfile load error."
                    isLoadingProfile = false
                }
            })
            onDispose { userProfileRef.removeEventListener(profileListener) }
        }

        // DisposableEffect for Transactions List
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (Transactions): Setting up for user $userId")
            isLoadingTransactions = true
            val userTransactionsNodeRef = userProfileRef.child("transactions")
            val transactionsListQuery = userTransactionsNodeRef.orderByChild("timestamp")
            val transactionListListener = transactionsListQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { /* ... update states, isLoadingTransactions = false ... */
                    Log.d(TAG_DASHBOARD_SCREEN, "Transactions list data received for $userId. Count: ${snapshot.childrenCount}")
                    val newTransactions = mutableListOf<Transaction>()
                    snapshot.children.forEach { data ->
                        data.getValue(Transaction::class.java)?.let {
                            it.id = data.key ?: ""
                            newTransactions.add(it)
                        }
                    }
                    transactionsList = newTransactions.reversed()
                    isLoadingTransactions = false
                }
                override fun onCancelled(error: DatabaseError) { /* ... handle error, isLoadingTransactions = false ... */
                    Log.e(TAG_DASHBOARD_SCREEN, "Transactions list onCancelled for $userId: ${error.message}", error.toException())
                    errorMessage = (errorMessage ?: "") + "\nTransactions load error."
                    isLoadingTransactions = false
                }
            })
            onDispose { transactionsListQuery.removeEventListener(transactionListListener) }
        }

        // DisposableEffect for "Earned this month"
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (EarnedMonth): Setting up for user $userId")
            isLoadingEarnedThisMonth = true
            SavingsCalculator.calculateInterestEarnedLastMonth(object : SavingsCalculator.InterestCalculationCallback {
                override fun onSuccess(totalInterest: Double, currency: String) { /* ... update states, isLoadingEarnedThisMonth = false ... */
                    Log.i(TAG_DASHBOARD_SCREEN, "SUCCESS 'Earned this month': $totalInterest $currency (User: $userId)")
                    earnedThisMonth = totalInterest
                    earnedThisMonthCurrency = currency
                    isLoadingEarnedThisMonth = false
                }
                override fun onError(errorMsg: String) { /* ... handle error, isLoadingEarnedThisMonth = false ... */
                    Log.e(TAG_DASHBOARD_SCREEN, "ERROR 'Earned this month': $errorMsg (User: $userId)")
                    errorMessage = (errorMessage ?: "") + "\nMonthly earnings error."
                    isLoadingEarnedThisMonth = false
                }
            })
            onDispose { /* No listener removal needed for single event calculator method */ }
        }

        // DisposableEffect for "Progress this month"
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (ProgressMonth): Setting up for user $userId")
            isLoadingProgressThisMonth = true
            SavingsCalculator.calculateProgressThisMonth(object : SavingsCalculator.MonthlyProgressCallback {
                override fun onSuccess(totalProgress: Double, currency: String) { /* ... update states, isLoadingProgressThisMonth = false ... */
                    Log.i(TAG_DASHBOARD_SCREEN, "SUCCESS 'Progress this month': $totalProgress $currency (User: $userId)")
                    progressThisMonth = totalProgress
                    progressThisMonthCurrency = currency
                    isLoadingProgressThisMonth = false
                }
                override fun onError(errorMsg: String) { /* ... handle error, isLoadingProgressThisMonth = false ... */
                    Log.e(TAG_DASHBOARD_SCREEN, "ERROR 'Progress this month': $errorMsg (User: $userId)")
                    errorMessage = (errorMessage ?: "") + "\nMonthly progress error."
                    isLoadingProgressThisMonth = false
                }
            })
            onDispose { /* No listener removal needed for single event calculator method */ }
        }
    }


    DashboardContent(
        transactions = transactionsList,
        totalSavings = totalSavings,
        savingsPercentage = savingsPercentage,
        earnedThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", earnedThisMonth, earnedThisMonthCurrency),
        progressThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", progressThisMonth, progressThisMonthCurrency),
        isLoading = isLoadingOverall,
        errorMessage = errorMessage,
        onLogout = {
            Log.e(TAG_DASHBOARD_SCREEN, "!!! LOGOUT BUTTON CLICKED !!!")
            Log.i(TAG_DASHBOARD_SCREEN, "Logout initiated. Calling auth.signOut().")
            auth.signOut() // This will trigger the AuthStateListener
        },
        onWithdrawClicked = {
            Log.d(TAG_DASHBOARD_SCREEN, "Withdraw clicked, navigating to Withdraw screen.")
            navController.navigate(Screen.Withdraw.route)
        }
    )
}