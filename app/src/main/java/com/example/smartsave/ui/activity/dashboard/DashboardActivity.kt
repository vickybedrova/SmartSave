package com.example.smartsave.ui.activity.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.smartsave.model.Transaction // Your JAVA Transaction model
import com.example.smartsave.ui.theme.SmartSaveTheme // Your app's theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : ComponentActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userProfileListener: ValueEventListener? = null
    private var transactionsListener: ValueEventListener? = null
    private var userProfileRef: DatabaseReference? = null
    // CORRECTED TYPE HERE:
    private var userTransactionsQuery: Query? = null // Changed from DatabaseReference? to Query?

    var transactionsList by mutableStateOf<List<Transaction>>(emptyList())
    var totalSavings by mutableStateOf(0.0)
    var savingsPercentage by mutableStateOf(0.0)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")

        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            Log.w("DashboardActivity", "No user logged in, redirecting to Login.")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            SmartSaveTheme {
                DashboardContent(
                    transactions = transactionsList,
                    totalSavings = totalSavings,
                    savingsPercentage = savingsPercentage,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogout = { logoutUser() },
                    onWithdrawClicked = { /* TODO: Implement withdraw functionality */ }
                )
            }
        }
        fetchUserData(currentUser.uid)
    }

    private fun fetchUserData(userId: String) {
        isLoading = true
        errorMessage = null
        Log.d("DashboardActivity", "Fetching data for user: $userId")

        userProfileRef = database.reference.child("smartSaveProfile").child(userId)

        userProfileListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalSavings = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0
                savingsPercentage = snapshot.child("savingsPercentage").getValue(Double::class.java) ?: 0.0
                Log.d("DashboardActivity", "Profile data updated: TotalSavings = $totalSavings, Percentage = $savingsPercentage")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("DashboardActivity", "Failed to read user profile data: ${error.message}")
                errorMessage = "Failed to load profile data."
            }
        }
        userProfileRef?.addValueEventListener(userProfileListener!!)

        // Assigning to the Query type variable
        userTransactionsQuery = userProfileRef!!.child("transactions").orderByChild("timestamp")

        transactionsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newTransactions = mutableListOf<Transaction>()
                snapshot.children.forEach { dataSnapshot ->
                    val transaction = dataSnapshot.getValue(Transaction::class.java)
                    transaction?.let {
                        it.id = dataSnapshot.key ?: ""
                        newTransactions.add(it)
                    }
                }
                transactionsList = newTransactions.reversed()
                isLoading = false
                errorMessage = null
                Log.d("DashboardActivity", "Transactions loaded: ${transactionsList.size}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("DashboardActivity", "loadTransactions:onCancelled", databaseError.toException())
                isLoading = false
                errorMessage = "Failed to load transactions: ${databaseError.message}"
            }
        }
        // Add listener to the Query object
        userTransactionsQuery?.addValueEventListener(transactionsListener!!)
    }

    private fun logoutUser() {
        mAuth.signOut()
        removeListeners()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun removeListeners() {
        userProfileListener?.let { listener ->
            userProfileRef?.removeEventListener(listener)
            userProfileListener = null
        }
        transactionsListener?.let { listener ->
            // Remove listener from the Query object
            userTransactionsQuery?.removeEventListener(listener)
            transactionsListener = null
        }
        // It's also good to null out the references themselves if they are nullable
        userProfileRef = null
        userTransactionsQuery = null
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListeners()
    }
}

