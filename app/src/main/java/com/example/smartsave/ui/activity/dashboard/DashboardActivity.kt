//package com.example.smartsave.ui.activity.dashboard
//
//import android.util.Log
//import androidx.compose.runtime.*
//import androidx.compose.ui.platform.LocalContext
//import androidx.navigation.NavController
//import com.example.smartsave.DashboardContent
//import com.example.smartsave.model.Transaction
//import com.example.smartsave.ui.navigation.Screen
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//
//@Composable
//fun DashboardScreen(navController: NavController) {
//    val context = LocalContext.current
//    val auth = remember { FirebaseAuth.getInstance() }
//    val database = remember {
//        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
//    }
//
//    val currentUser = auth.currentUser
//
//    var transactionsList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
//    var totalSavings by remember { mutableStateOf(0.0) }
//    var savingsPercentage by remember { mutableStateOf(0.0) }
//    var isLoading by remember { mutableStateOf(true) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    LaunchedEffect(currentUser) {
//        if (currentUser == null) {
//            navController.navigate(Screen.Login.route) {
//                popUpTo(0)
//            }
//        } else {
//            val userId = currentUser.uid
//            val userProfileRef = database.reference.child("smartSaveProfile").child(userId)
//
//            userProfileRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    totalSavings = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0
//                    savingsPercentage = snapshot.child("savingsPercentage").getValue(Double::class.java) ?: 0.0
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    errorMessage = "Failed to load profile data: ${error.message}"
//                }
//            })
//
//            val userTransactionsQuery = userProfileRef.child("transactions").orderByChild("timestamp")
//            userTransactionsQuery.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val newTransactions = mutableListOf<Transaction>()
//                    snapshot.children.forEach { data ->
//                        data.getValue(Transaction::class.java)?.let {
//                            it.id = data.key ?: ""
//                            newTransactions.add(it)
//                        }
//                    }
//                    transactionsList = newTransactions.reversed()
//                    isLoading = false
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    errorMessage = "Failed to load transactions: ${error.message}"
//                    isLoading = false
//                }
//            })
//        }
//    }
//
//    DashboardContent(
//        transactions = transactionsList,
//        totalSavings = totalSavings,
//        savingsPercentage = savingsPercentage,
//        isLoading = isLoading,
//        errorMessage = errorMessage,
//        onLogout = {
//            auth.signOut()
//            navController.navigate(Screen.Login.route) {
//                popUpTo(0)
//            }
//        },
//        onWithdrawClicked = {
//            navController.navigate(Screen.Withdraw.route)
//        }
//    )
//}