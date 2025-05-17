package com.example.smartsave.ui.activity.dashboard

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartsave.DashboardContent
import com.example.smartsave.model.Transaction
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.util.SavingsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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

    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var totalSavings by remember { mutableStateOf(0.0) }
    var pendingWithdrawalAmount by remember { mutableStateOf(0.0) }
    var savingsPercentage by remember { mutableStateOf(0.0) }
    var earnedThisMonth by remember { mutableStateOf(0.0) }
    var earnedThisMonthCurrency by remember { mutableStateOf("BGN") }
    var progressThisMonth by remember { mutableStateOf(0.0) }
    var progressThisMonthCurrency by remember { mutableStateOf("BGN") }
    var isSmartSaveActive by remember { mutableStateOf(true) }

    var isLoadingProfile by remember { mutableStateOf(true) }
    var isLoadingTransactions by remember { mutableStateOf(true) }
    var isLoadingEarnedThisMonth by remember { mutableStateOf(true) }
    var isLoadingProgressThisMonth by remember { mutableStateOf(true) }
    var isTogglingActiveState by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoadingOverall = isLoadingProfile || isLoadingTransactions || isLoadingEarnedThisMonth || isLoadingProgressThisMonth

    var pendingText by remember { mutableStateOf<String?>(null) }

    DisposableEffect(key1 = auth) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (currentAuthUser?.uid != user?.uid) {
                currentAuthUser = user
            }
        }
        auth.addAuthStateListener(authStateListener)
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    LaunchedEffect(key1 = currentAuthUser) {
        if (currentAuthUser == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            isLoadingProfile = true
            isLoadingTransactions = true
            isLoadingEarnedThisMonth = true
            isLoadingProgressThisMonth = true
            errorMessage = null
            transactionsList = emptyList()
            totalSavings = 0.0
            savingsPercentage = 0.0
            earnedThisMonth = 0.0
            earnedThisMonthCurrency = "BGN"
            progressThisMonth = 0.0
            progressThisMonthCurrency = "BGN"
            selectedTransactionFilter = TransactionFilter.ALL
        }
    }

    currentAuthUser?.let { user ->
        val userId = user.uid
        val userProfileRef = database.reference.child("smartSaveProfile").child(userId)

        DisposableEffect(key1 = userId) {
            isLoadingProfile = true
            val profileListener = userProfileRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalSavings = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0
                    savingsPercentage = snapshot.child("savingsPercentage").getValue(Double::class.java) ?: 0.0
                    isLoadingProfile = false
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = (errorMessage ?: "") + "\nProfile load error."
                    isLoadingProfile = false
                }
            })
            onDispose { userProfileRef.removeEventListener(profileListener) }
        }

        DisposableEffect(key1 = userId, key2 = selectedTransactionFilter) {
            isLoadingTransactions = true
            transactionsList = emptyList()
            errorMessage = null

            val userTransactionsNodeRef = userProfileRef.child("transactions")
            var query: Query = userTransactionsNodeRef.orderByChild("date")

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            when (selectedTransactionFilter) {
                TransactionFilter.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startTodayTimestamp = calendar.timeInMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val endTodayTimestamp = calendar.timeInMillis
                    query = userTransactionsNodeRef.orderByChild("timestamp")
                        .startAt(startTodayTimestamp.toDouble())
                        .endAt(endTodayTimestamp.toDouble())
                }

                TransactionFilter.THIS_WEEK -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val endOfWeekTimestamp = calendar.timeInMillis
                    val tempCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    tempCal.set(Calendar.DAY_OF_WEEK, tempCal.firstDayOfWeek)
                    tempCal.set(Calendar.HOUR_OF_DAY, 0)
                    tempCal.set(Calendar.MINUTE, 0)
                    tempCal.set(Calendar.SECOND, 0)
                    tempCal.set(Calendar.MILLISECOND, 0)
                    val startOfWeekTimestamp = tempCal.timeInMillis
                    query = userTransactionsNodeRef.orderByChild("timestamp")
                        .startAt(startOfWeekTimestamp.toDouble())
                        .endAt(endOfWeekTimestamp.toDouble())
                }

                TransactionFilter.ALL -> {}
            }

            val transactionListListener = query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newTransactions = mutableListOf<Transaction>()
                    snapshot.children.forEach { data ->
                        data.getValue(Transaction::class.java)?.let {
                            it.id = data.key ?: ""
                            newTransactions.add(it)
                        }
                    }
                    transactionsList = newTransactions.reversed()

                    pendingWithdrawalAmount = newTransactions
                        .filter { it.type == "PENDING_WITHDRAWAL" }
                        .sumOf { it.amount }

                    pendingText = if (pendingWithdrawalAmount != 0.0)
                        String.format(Locale.getDefault(), "%.2f BGN is being withdrawn...", -pendingWithdrawalAmount)
                    else null

                    isLoadingTransactions = false
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = (errorMessage ?: "") + "\nFailed to load transactions."
                    isLoadingTransactions = false
                }
            })

            onDispose { query.removeEventListener(transactionListListener) }
        }

        DisposableEffect(key1 = userId) {
            isLoadingEarnedThisMonth = true
            SavingsCalculator.calculateInterestEarnedLastMonth(object : SavingsCalculator.InterestCalculationCallback {
                override fun onSuccess(totalInterest: Double, currency: String) {
                    earnedThisMonth = totalInterest
                    earnedThisMonthCurrency = "BGN"
                    isLoadingEarnedThisMonth = false
                }

                override fun onError(errorMsg: String) {
                    errorMessage = (errorMessage ?: "") + "\nMonthly earnings error."
                    isLoadingEarnedThisMonth = false
                }
            })
            onDispose { }
        }

        DisposableEffect(key1 = userId) {
            isLoadingProgressThisMonth = true
            SavingsCalculator.calculateProgressThisMonth(object : SavingsCalculator.MonthlyProgressCallback {
                override fun onSuccess(totalProgress: Double, currency: String) {
                    progressThisMonth = totalProgress
                    progressThisMonthCurrency = "BGN"
                    isLoadingProgressThisMonth = false
                }

                override fun onError(errorMsg: String) {
                    errorMessage = (errorMessage ?: "") + "\nMonthly progress error."
                    isLoadingProgressThisMonth = false
                }
            })
            onDispose { }
        }
    }

    DashboardContent(
        transactions = transactionsList,
        totalSavings = totalSavings,
        savingsPercentage = savingsPercentage,
        earnedThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", earnedThisMonth, earnedThisMonthCurrency),
        progressThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", progressThisMonth, progressThisMonthCurrency),
        selectedFilter = selectedTransactionFilter,
        onFilterSelected = { newFilter -> selectedTransactionFilter = newFilter },
        pendingWithdrawalMessage = pendingText,
        isSmartSaveActive = isSmartSaveActive,
        onToggleActiveState = {
            if (currentAuthUser == null || isTogglingActiveState) return@DashboardContent
            val userId = currentAuthUser!!.uid
            val profileIsActiveRef = database.reference.child("smartSaveProfile").child(userId).child("isActive")

            val currentLocalState = isSmartSaveActive
            val newActiveState = !currentLocalState

            isTogglingActiveState = true
            isSmartSaveActive = newActiveState

            profileIsActiveRef.setValue(newActiveState)
                .addOnSuccessListener {
                    Toast.makeText(context, "SmartSave ${if (newActiveState) "Resumed" else "Paused"}", Toast.LENGTH_SHORT).show()
                    isTogglingActiveState = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                    isSmartSaveActive = currentLocalState
                    isTogglingActiveState = false
                }
        },
        isLoading = isLoadingOverall,
        errorMessage = errorMessage,
        onLogout = {
            auth.signOut()
            currentAuthUser = null
        },
        onWithdrawClicked = { navController.navigate(Screen.Withdraw.route) },
        onAdjustClicked = { navController.navigate(Screen.Setup.route) },
        onAnalyticsClicked = { navController.navigate(Screen.Analytics.route) }
    )
}