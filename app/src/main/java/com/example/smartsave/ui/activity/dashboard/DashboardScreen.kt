package com.example.smartsave.ui.activity.dashboard

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartsave.DashboardContent
import com.example.smartsave.data.RetrofitClient
import com.example.smartsave.model.Transaction
import com.example.smartsave.data.mappers.toDomainModel
import com.example.smartsave.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.example.smartsave.data.TokenManager // <<< NEW >>> Import TokenManager

private const val TAG_DASHBOARD_SCREEN = "DashboardScreenLogicAPI"

// <<< REMOVED >>> API Token constants are no longer defined here globally.
// private const val MYPOS_SESSION_TOKEN = "..."
// private const val MYPOS_AUTHORIZATION_TOKEN = "..."
private const val MYPOS_PAGE_SIZE = 50 // This can stay if it's a fixed preference

enum class TransactionFilter {
    ALL, TODAY, THIS_WEEK
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedTransactionFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var allFetchedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    // Other data remains hardcoded for this step
    val totalSavings by remember { mutableStateOf(1234.56) }
    val savingsPercentage by remember { mutableStateOf(10.0) }
    var isSmartSaveActive by remember { mutableStateOf(true) }
    val earnedThisMonth by remember { mutableStateOf(25.50) }
    val earnedThisMonthCurrency by remember { mutableStateOf("EUR") }
    val progressThisMonth by remember { mutableStateOf(150.75) }
    val progressThisMonthCurrency by remember { mutableStateOf("EUR") }

    val isLoadingProfile by remember { mutableStateOf(false) }
    var isLoadingTransactions by remember { mutableStateOf(true) } // Initially true
    val isLoadingEarnedThisMonth by remember { mutableStateOf(false) }
    val isLoadingProgressThisMonth by remember { mutableStateOf(false) }
    var isTogglingActiveState by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoadingOverall = isLoadingTransactions || isLoadingEarnedThisMonth || isLoadingProgressThisMonth || isLoadingProfile

    val coroutineScope = rememberCoroutineScope()

    // --- <<< NEW >>> Load Tokens ---
    var sessionTokenState by remember { mutableStateOf<String?>(null) }
    var authorizationHeaderState by remember { mutableStateOf<String?>(null) }
    var tokensAreLoaded by remember { mutableStateOf(false) } // To track if token loading attempt is complete

    LaunchedEffect(Unit) { // Runs once when the composable enters composition
        Log.d(TAG_DASHBOARD_SCREEN, "Attempting to load tokens from TokenManager.")
        sessionTokenState = TokenManager.getSessionToken(context)
        authorizationHeaderState = TokenManager.getAuthorizationHeader(context)
        tokensAreLoaded = true // Mark that we've attempted to load tokens

        if (sessionTokenState == null || authorizationHeaderState == null) {
            Log.w(TAG_DASHBOARD_SCREEN, "Tokens not found after loading attempt. Navigating to Login.")
            // Ensure navigation happens only once if tokens are truly missing
            if (navController.currentDestination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            Log.i(TAG_DASHBOARD_SCREEN, "Tokens loaded successfully.")
        }
    }
    // --- <<< END NEW >>> Load Tokens ---

    val currentFilterAndSetTransactions by rememberUpdatedState(
    ) { allTxs: List<Transaction>, filter: TransactionFilter ->
        val shouldSetBriefLoading = !isLoadingTransactions && allTxs.isNotEmpty()
        if (shouldSetBriefLoading) isLoadingTransactions = true

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val filtered = when (filter) {
            TransactionFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
                val startTodayTimestamp = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
                val endTodayTimestamp = calendar.timeInMillis
                allTxs.filter { it.timestamp in startTodayTimestamp..endTodayTimestamp }
            }
            TransactionFilter.THIS_WEEK -> {
                val tempCalEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                tempCalEnd.set(Calendar.HOUR_OF_DAY, 23); tempCalEnd.set(Calendar.MINUTE, 59); tempCalEnd.set(Calendar.SECOND, 59); tempCalEnd.set(Calendar.MILLISECOND, 999)
                val endOfWeekTimestamp = tempCalEnd.timeInMillis
                val tempCalStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                tempCalStart.set(Calendar.DAY_OF_WEEK, tempCalStart.firstDayOfWeek)
                tempCalStart.set(Calendar.HOUR_OF_DAY, 0); tempCalStart.set(Calendar.MINUTE, 0); tempCalStart.set(Calendar.SECOND, 0); tempCalStart.set(Calendar.MILLISECOND, 0)
                val startOfWeekTimestamp = tempCalStart.timeInMillis
                allTxs.filter { it.timestamp in startOfWeekTimestamp..endOfWeekTimestamp }
            }
            TransactionFilter.ALL -> allTxs
        }
        transactionsList = filtered.sortedByDescending { it.timestamp }
        Log.d(TAG_DASHBOARD_SCREEN, "Applied filter $filter. Result count: ${transactionsList.size}")

        if (shouldSetBriefLoading) isLoadingTransactions = false
    }

    // --- <<< MODIFIED >>> API Fetching Logic - Keyed to dynamically loaded tokens ---
    LaunchedEffect(key1 = sessionTokenState, key2 = authorizationHeaderState, key3 = tokensAreLoaded) {
        if (!tokensAreLoaded) {
            // Still waiting for the initial token load attempt to complete.
            // isLoadingTransactions should remain true or be set by the token loader.
            Log.d(TAG_DASHBOARD_SCREEN, "Tokens not loaded yet, API call deferred.")
            // Ensure isLoadingTransactions reflects this uncertainty until tokens are confirmed
            if (!isLoadingTransactions) isLoadingTransactions = true
            return@LaunchedEffect
        }

        if (sessionTokenState == null || authorizationHeaderState == null) {
            Log.w(TAG_DASHBOARD_SCREEN, "Tokens are missing. Cannot fetch transactions (API call block).")
            if (errorMessage == null) { // Avoid overwriting a more specific error from token loading
                errorMessage = "Session missing. Please log in."
            }
            isLoadingTransactions = false // No API call will be made
            allFetchedTransactions = emptyList()
            transactionsList = emptyList()
            return@LaunchedEffect
        }

        Log.d(TAG_DASHBOARD_SCREEN, "Attempting to fetch transactions with loaded tokens.")
        isLoadingTransactions = true
        errorMessage = null
        // Optionally clear lists, or let them persist if re-fetching on token change
        // allFetchedTransactions = emptyList()
        // transactionsList = emptyList()

        coroutineScope.launch {
            try {
                // Use the state variables for tokens
                val currentSessionToken = sessionTokenState!!
                val currentAuthHeader = authorizationHeaderState!!

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getTransactions(
                        session = currentSessionToken,
                        authorization = currentAuthHeader,
                        page = 1,
                        pageSize = MYPOS_PAGE_SIZE
                    ) // <--- .execute() removed
                }

                if (response.isSuccessful) {
                    val transactionResponse = response.body()
                    if (transactionResponse != null) {
                        Log.i(TAG_DASHBOARD_SCREEN, "API call successful. Items: ${transactionResponse.items.size}")
                        val fetchedTxs = transactionResponse.items.map { it.toDomainModel() }
                        allFetchedTransactions = fetchedTxs
                    } else {
                        Log.e(TAG_DASHBOARD_SCREEN, "API response body is null.")
                        errorMessage = "Failed to parse transactions (empty API response)."
                        allFetchedTransactions = emptyList()
                    }
                } else {
                    val errorBodyString = response.errorBody()?.use { it.string() } ?: "Unknown API error"
                    Log.e(TAG_DASHBOARD_SCREEN, "API Error ${response.code()}: $errorBodyString")
                    errorMessage = "API Error ${response.code()}: Could not fetch transactions."
                    allFetchedTransactions = emptyList()
                    if (response.code() == 401 || response.code() == 403) { // Unauthorized or Forbidden
                        Log.w(TAG_DASHBOARD_SCREEN, "Token might be invalid (401/403). Clearing tokens and navigating to Login.")
                        TokenManager.clearTokens(context)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG_DASHBOARD_SCREEN, "Network error: ${e.message}", e)
                errorMessage = "Network error. Please check connection."
                allFetchedTransactions = emptyList()
            } catch (e: Exception) {
                Log.e(TAG_DASHBOARD_SCREEN, "Unexpected error: ${e.message}", e)
                errorMessage = "An unexpected error occurred."
                allFetchedTransactions = emptyList()
            } finally {
                isLoadingTransactions = false
            }
        }
    }

    // Effect to re-apply filter
    LaunchedEffect(key1 = selectedTransactionFilter, key2 = allFetchedTransactions, key3 = tokensAreLoaded) {
        if (tokensAreLoaded && sessionTokenState != null && authorizationHeaderState != null) {
            // Only filter if tokens were loaded successfully and we have a valid session
            Log.d(TAG_DASHBOARD_SCREEN, "Filter or base data changed. Re-filtering. Base count: ${allFetchedTransactions.size}")
            currentFilterAndSetTransactions(allFetchedTransactions, selectedTransactionFilter)
        } else if (tokensAreLoaded && (sessionTokenState == null || authorizationHeaderState == null)) {
            // If tokens failed to load, ensure lists are empty
            transactionsList = emptyList()
            Log.d(TAG_DASHBOARD_SCREEN, "Tokens invalid/missing, ensuring transaction list is empty.")
        }
    }

    DashboardContent(
        transactions = transactionsList,
        totalSavings = totalSavings,
        savingsPercentage = savingsPercentage,
        earnedThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", earnedThisMonth, earnedThisMonthCurrency),
        progressThisMonthValue = String.format(Locale.getDefault(), "%.2f %s", progressThisMonth, progressThisMonthCurrency),
        selectedFilter = selectedTransactionFilter,
        onFilterSelected = { newFilter ->
            if (selectedTransactionFilter != newFilter) {
                selectedTransactionFilter = newFilter
            }
        },
        isSmartSaveActive = isSmartSaveActive,
        onToggleActiveState = {
            if (isTogglingActiveState) return@DashboardContent
            isTogglingActiveState = true
            val newActiveState = !isSmartSaveActive
            isSmartSaveActive = newActiveState
            Toast.makeText(context, "SmartSave ${if (newActiveState) "Resumed" else "Paused"}", Toast.LENGTH_SHORT).show()
            isTogglingActiveState = false
        },
        isLoading = isLoadingOverall,
        errorMessage = errorMessage,
        onLogout = {
            Log.i(TAG_DASHBOARD_SCREEN, "Logout clicked. Clearing tokens and navigating to Login.")
            TokenManager.clearTokens(context) // <<< MODIFIED >>> Use TokenManager to clear
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            // Ensure we navigate out of the dashboard cleanly
            if (navController.currentDestination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        onWithdrawClicked = { navController.navigate(Screen.Withdraw.route) },
        onAdjustClicked = { navController.navigate(Screen.Setup.route) },
        onAnalyticsClicked = { navController.navigate(Screen.Analytics.route) }
    )
}