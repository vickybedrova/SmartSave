package com.example.smartsave.ui.activity.dashboard

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.smartsave.DashboardContent
import com.example.smartsave.model.Transaction
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.util.SavingsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale


private const val TAG_DASHBOARD_SCREEN = "DashboardScreenLogic"

@Composable
fun DashboardScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember {
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    }
    val currentUser = auth.currentUser

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

    val isLoadingOverall = isLoadingProfile || isLoadingTransactions || isLoadingEarnedThisMonth || isLoadingProgressThisMonth // Updated

    LaunchedEffect(key1 = currentUser) {
        if (currentUser == null) {
            Log.w(TAG_DASHBOARD_SCREEN, "No current user, navigating to Login.")
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else {
            Log.d(TAG_DASHBOARD_SCREEN, "User ${currentUser.uid} detected. Resetting loading states.")
            isLoadingProfile = true
            isLoadingTransactions = true
            isLoadingEarnedThisMonth = true
            isLoadingProgressThisMonth = true // Reset new loading state
            errorMessage = null
            transactionsList = emptyList()
            totalSavings = 0.0
            savingsPercentage = 0.0
            earnedThisMonth = 0.0
            earnedThisMonthCurrency = "EUR"
            progressThisMonth = 0.0 // Reset new state
            progressThisMonthCurrency = "EUR" // Reset new state
        }
    }

    if (currentUser != null) {
        val userId = currentUser.uid
        val userProfileRef = database.reference.child("smartSaveProfile").child(userId)

        // DisposableEffect for Profile Data
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Setting up profile listener for user $userId")
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
            onDispose {
                Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Removing profile listener for user $userId")
                userProfileRef.removeEventListener(profileListener)
            }
        }

        // DisposableEffect for Transactions List
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Setting up transactions list listener for user $userId")
            isLoadingTransactions = true
            val userTransactionsNodeRef = userProfileRef.child("transactions")
            val transactionsListQuery = userTransactionsNodeRef.orderByChild("timestamp")
            val transactionListListener = transactionsListQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG_DASHBOARD_SCREEN, "Transactions list onCancelled for $userId: ${error.message}", error.toException())
                    errorMessage = (errorMessage ?: "") + "\nTransactions load error."
                    isLoadingTransactions = false
                }
            })
            onDispose {
                Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Removing transactions list listener for user $userId")
                transactionsListQuery.removeEventListener(transactionListListener)
            }
        }

        // DisposableEffect for "Earned this month"
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Triggering 'Earned this month' calculation for user $userId")
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
            onDispose { /* No listener to remove from single event in calculator */ }
        }

        // DisposableEffect for "Progress this month"
        DisposableEffect(key1 = userId) {
            Log.d(TAG_DASHBOARD_SCREEN, "DisposableEffect: Triggering 'Progress this month' calculation for user $userId")
            isLoadingProgressThisMonth = true // Set loading true

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
            onDispose { /* No listener to remove from single event in calculator */ }
        }

    } // End of if (currentUser != null)

    DashboardContent(
        transactions = transactionsList,
        totalSavings = totalSavings,
        savingsPercentage = savingsPercentage,
        earnedThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", earnedThisMonth, earnedThisMonthCurrency),
        progressThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", progressThisMonth, progressThisMonthCurrency), // <<< NEW
        isLoading = isLoadingOverall,
        errorMessage = errorMessage,
        onLogout = {
            Log.i(TAG_DASHBOARD_SCREEN, "Logout initiated.")
            auth.signOut()
        },
        onWithdrawClicked = {
            Log.d(TAG_DASHBOARD_SCREEN, "Withdraw clicked, navigating to Withdraw screen.")
            navController.navigate(Screen.Withdraw.route)
        }
    )
}