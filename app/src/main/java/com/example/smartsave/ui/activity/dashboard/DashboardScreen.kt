package com.example.smartsave.ui.activity.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartsave.DashboardContent
import com.example.smartsave.model.Transaction
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.util.SavingsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val TAG_DASHBOARD_SCREEN = "DashboardScreenLogic"

enum class TransactionFilter {
    ALL, TODAY, THIS_WEEK
}

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember {
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    }
    var currentAuthUser by remember { mutableStateOf(auth.currentUser) }

    var selectedTransactionFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    // States for data
    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var totalSavings by remember { mutableStateOf(0.0) }
    var savingsPercentage by remember { mutableStateOf(0.0) }
    // ... (earnedThisMonth, progressThisMonth states remain the same) ...
    var earnedThisMonth by remember { mutableStateOf(0.0) }
    var earnedThisMonthCurrency by remember { mutableStateOf("EUR") }
    var progressThisMonth by remember { mutableStateOf(0.0) }
    var progressThisMonthCurrency by remember { mutableStateOf("EUR") }
    var isSmartSaveActive by remember { mutableStateOf(true) } // Default, will be fetched



    // Granular Loading States
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isLoadingTransactions by remember { mutableStateOf(true) } // This will now also depend on the filter
    // ... (isLoadingEarnedThisMonth, isLoadingProgressThisMonth remain the same) ...
    var isLoadingEarnedThisMonth by remember { mutableStateOf(true) }
    var isLoadingProgressThisMonth by remember { mutableStateOf(true) }
    var isTogglingActiveState by remember { mutableStateOf(false) }



    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoadingOverall = isLoadingProfile || isLoadingTransactions || isLoadingEarnedThisMonth || isLoadingProgressThisMonth

    // AuthStateListener DisposableEffect (remains the same)
    DisposableEffect(key1 = auth) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth -> /* ... */
            val user = firebaseAuth.currentUser
            Log.d(TAG_DASHBOARD_SCREEN, "AuthStateListener: User changed to: ${user?.uid}")
            if (currentAuthUser?.uid != user?.uid) {
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

    // LaunchedEffect for user state changes (login/logout, initial data reset)
    LaunchedEffect(key1 = currentAuthUser) {
        Log.d(TAG_DASHBOARD_SCREEN, "LaunchedEffect triggered by currentAuthUser. UID: ${currentAuthUser?.uid}")
        if (currentAuthUser == null) {
            // ... (navigation to Login - remains the same) ...
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
            // User is logged in, reset states (this will trigger DisposableEffects to fetch)
            val userId = currentAuthUser!!.uid
            Log.d(TAG_DASHBOARD_SCREEN, "User $userId detected. Resetting loading states and data.")
            isLoadingProfile = true
            isLoadingTransactions = true // Will be set by transactions DisposableEffect
            isLoadingEarnedThisMonth = true
            isLoadingProgressThisMonth = true
            errorMessage = null
            transactionsList = emptyList() // Clear previous list
            totalSavings = 0.0
            savingsPercentage = 0.0
            earnedThisMonth = 0.0
            earnedThisMonthCurrency = "EUR"
            progressThisMonth = 0.0
            progressThisMonthCurrency = "EUR"
            selectedTransactionFilter = TransactionFilter.ALL // Default to ALL on new user/refresh
        }
    }

    // Data fetching DisposableEffects
    currentAuthUser?.let { user ->
        val userId = user.uid
        val userProfileRef = database.reference.child("smartSaveProfile").child(userId)

        // Profile Data DisposableEffect (remains mostly the same)
        DisposableEffect(key1 = userId) {
            // ... (profile data fetching logic - no changes needed here for filters) ...
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (Profile): Setting up for user $userId")
            isLoadingProfile = true
            val profileListener = userProfileRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG_DASHBOARD_SCREEN, "Profile data received for $userId.")
                    totalSavings = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0
                    savingsPercentage = snapshot.child("savingsPercentage").getValue(Double::class.java) ?: 0.0
                    isLoadingProfile = false
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG_DASHBOARD_SCREEN, "Profile data onCancelled for $userId: ${error.message}", error.toException())
                    errorMessage = (errorMessage ?: "") + "\nProfile load error."
                    isLoadingProfile = false
                }
            })
            onDispose { userProfileRef.removeEventListener(profileListener) }
        }


        // --- MODIFIED: DisposableEffect for Transactions List (reacts to filter) ---
        DisposableEffect(key1 = userId, key2 = selectedTransactionFilter) { // Re-run if userId OR filter changes
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (Transactions): Setting up for user $userId, Filter: $selectedTransactionFilter")
            isLoadingTransactions = true
            transactionsList = emptyList() // Clear previous list when filter changes
            errorMessage = null // Clear previous transaction-specific errors

            val userTransactionsNodeRef = userProfileRef.child("transactions")
            var query: Query = userTransactionsNodeRef.orderByChild("timestamp") // Default query

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            var filterMessage = "No transactions yet." // Default empty message

            when (selectedTransactionFilter) {
                TransactionFilter.TODAY -> {
                    filterMessage = "No transactions from today."
                    // Start of today
                    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
                    val startTodayTimestamp = calendar.timeInMillis
                    // End of today
                    calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
                    val endTodayTimestamp = calendar.timeInMillis
                    query = userTransactionsNodeRef.orderByChild("timestamp")
                        .startAt(startTodayTimestamp.toDouble())
                        .endAt(endTodayTimestamp.toDouble())
                    Log.d(TAG_DASHBOARD_SCREEN, "TODAY Filter: $startTodayTimestamp to $endTodayTimestamp")
                }
                TransactionFilter.THIS_WEEK -> {
                    filterMessage = "No transactions from this week."
                    // End of today (effectively end of this week for the query up to now)
                    calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
                    val endOfWeekTimestamp = calendar.timeInMillis
                    // Start of this week (e.g., Sunday or Monday depending on locale, let's use ISO standard: Monday)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // Adjust if your week starts differently
                    // For ISO standard (Monday as first day):
                    // val isoCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")) // Fresh calendar for this
                    // isoCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    // To ensure it's the *current* week's Monday, not a future/past one if today is Sunday:
                    val tempCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")) // Use current day
                    tempCal.set(Calendar.DAY_OF_WEEK, tempCal.firstDayOfWeek) // Go to start of current week
                    tempCal.set(Calendar.HOUR_OF_DAY, 0); tempCal.set(Calendar.MINUTE, 0); tempCal.set(Calendar.SECOND, 0); tempCal.set(Calendar.MILLISECOND, 0)
                    val startOfWeekTimestamp = tempCal.timeInMillis

                    query = userTransactionsNodeRef.orderByChild("timestamp")
                        .startAt(startOfWeekTimestamp.toDouble())
                        .endAt(endOfWeekTimestamp.toDouble())
                    Log.d(TAG_DASHBOARD_SCREEN, "THIS_WEEK Filter: $startOfWeekTimestamp to $endOfWeekTimestamp")
                }
                TransactionFilter.ALL -> {
                    // Query remains userTransactionsNodeRef.orderByChild("timestamp")
                    Log.d(TAG_DASHBOARD_SCREEN, "ALL Filter selected.")
                }
            }

            val transactionListListener = query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG_DASHBOARD_SCREEN, "Filtered Txs data received for $userId. Filter: $selectedTransactionFilter. Count: ${snapshot.childrenCount}")
                    val newTransactions = mutableListOf<Transaction>()
                    snapshot.children.forEach { data ->
                        data.getValue(Transaction::class.java)?.let {
                            it.id = data.key ?: ""
                            newTransactions.add(it)
                        }
                    }
                    transactionsList = newTransactions.reversed() // Newest first
                    if (newTransactions.isEmpty() && selectedTransactionFilter != TransactionFilter.ALL) {
                        // Set specific empty message if a filter is active and results are empty
                        // This could be handled in DashboardContent too, but setting it here is an option
                        // For now, let DashboardContent handle display based on empty list and filter
                    }
                    isLoadingTransactions = false
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG_DASHBOARD_SCREEN, "Filtered Txs onCancelled for $userId: ${error.message}", error.toException())
                    errorMessage = (errorMessage ?: "") + "\nFailed to load transactions."
                    isLoadingTransactions = false
                }
            })
            onDispose {
                Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (Transactions): Removing listener for $userId, Filter: $selectedTransactionFilter")
                query.removeEventListener(transactionListListener)
            }
        }
        // --- END MODIFIED ---


        // DisposableEffect for "Earned this month" (remains the same)
        DisposableEffect(key1 = userId) {
            // ... (Earned this month logic - no changes needed here for filters on main list) ...
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (EarnedMonth): Setting up for user $userId")
            isLoadingEarnedThisMonth = true
            SavingsCalculator.calculateInterestEarnedLastMonth(object : SavingsCalculator.InterestCalculationCallback {
                override fun onSuccess(totalInterest: Double, currency: String) {
                    Log.i(TAG_DASHBOARD_SCREEN, "SUCCESS 'Earned this month': $totalInterest $currency (User: $userId)")
                    earnedThisMonth = totalInterest
                    earnedThisMonthCurrency = currency
                    isLoadingEarnedThisMonth = false
                }
                override fun onError(errorMsg: String) {
                    Log.e(TAG_DASHBOARD_SCREEN, "ERROR 'Earned this month': $errorMsg (User: $userId)")
                    errorMessage = (errorMessage ?: "") + "\nMonthly earnings error."
                    isLoadingEarnedThisMonth = false
                }
            })
            onDispose { /* No listener removal for single event */ }
        }

        // DisposableEffect for "Progress this month" (remains the same)
        DisposableEffect(key1 = userId) {
            // ... (Progress this month logic - no changes needed here for filters on main list) ...
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect (ProgressMonth): Setting up for user $userId")
            isLoadingProgressThisMonth = true
            SavingsCalculator.calculateProgressThisMonth(object : SavingsCalculator.MonthlyProgressCallback {
                override fun onSuccess(totalProgress: Double, currency: String) {
                    Log.i(TAG_DASHBOARD_SCREEN, "SUCCESS 'Progress this month': $totalProgress $currency (User: $userId)")
                    progressThisMonth = totalProgress
                    progressThisMonthCurrency = currency
                    isLoadingProgressThisMonth = false
                }
                override fun onError(errorMsg: String) {
                    Log.e(TAG_DASHBOARD_SCREEN, "ERROR 'Progress this month': $errorMsg (User: $userId)")
                    errorMessage = (errorMessage ?: "") + "\nMonthly progress error."
                    isLoadingProgressThisMonth = false
                }
            })
            onDispose { /* No listener removal for single event */ }
        }
    }

    DashboardContent(
        // ... (other existing parameters: transactions, totalSavings, etc.)
        transactions = transactionsList,
        totalSavings = totalSavings,
        savingsPercentage = savingsPercentage,
        earnedThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", earnedThisMonth, earnedThisMonthCurrency),
        progressThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", progressThisMonth, progressThisMonthCurrency),
        selectedFilter = selectedTransactionFilter,
        onFilterSelected = { newFilter -> selectedTransactionFilter = newFilter },

        // --- NEW: Pass active state and toggle handler ---
        isSmartSaveActive = isSmartSaveActive,
        onToggleActiveState = {
            if (currentAuthUser == null || isTogglingActiveState) {
                Log.w(TAG_DASHBOARD_SCREEN, "Toggle attempt ignored: no user or already toggling.")
                return@DashboardContent
            }
            val userId = currentAuthUser!!.uid
            val profileIsActiveRef = database.reference.child("smartSaveProfile").child(userId).child("isActive")

            // --- Calculate new state based on current state ---
            val currentLocalState = isSmartSaveActive
            val newActiveStateToSetInFirebase = !currentLocalState

            Log.d(TAG_DASHBOARD_SCREEN, "Attempting to toggle SmartSave from $currentLocalState to $newActiveStateToSetInFirebase for user $userId")
            isTogglingActiveState = true // Set loading state now

            // --- OPTIMISTICALLY UPDATE LOCAL STATE for faster UI response ---
            isSmartSaveActive = newActiveStateToSetInFirebase
            // --- END OPTIMISTIC UPDATE ---

            profileIsActiveRef.setValue(newActiveStateToSetInFirebase)
                .addOnSuccessListener {
                    Log.i(TAG_DASHBOARD_SCREEN, "Firebase: SmartSave active state successfully set to $newActiveStateToSetInFirebase for $userId")
                    // Local state is already optimistically updated.
                    // The listener will eventually get this same value and confirm it.
                    Toast.makeText(context, "SmartSave ${if (newActiveStateToSetInFirebase) "Resumed" else "Paused"}", Toast.LENGTH_SHORT).show()
                    isTogglingActiveState = false
                }
                .addOnFailureListener { e ->
                    Log.e(TAG_DASHBOARD_SCREEN, "Firebase: Failed to update SmartSave active state for $userId", e)
                    Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                    // --- REVERT OPTIMISTIC UPDATE ON FAILURE ---
                    isSmartSaveActive = currentLocalState // Revert to the state before the toggle attempt
                    // --- END REVERT ---
                    isTogglingActiveState = false
                }
        },
        // --- END NEW ---

        isLoading = isLoadingOverall,
        errorMessage = errorMessage,
        onLogout = { auth.signOut() },
        onWithdrawClicked = { navController.navigate(Screen.Withdraw.route) },
        onAdjustClicked = { navController.navigate(Screen.Setup.route) },
        onAnalyticsClicked = { navController.navigate(Screen.Analytics.route) }
    )
}