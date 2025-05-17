package com.example.smartsave.ui.activity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsave.R
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.ui.theme.blue
import com.example.smartsave.ui.theme.greyFieldBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WithdrawViewModel : ViewModel() {
    private val _totalSavings = MutableStateFlow<Double?>(null)
    val totalSavings: StateFlow<Double?> = _totalSavings
    private val _hasPendingWithdrawal = MutableStateFlow(false)
    val hasPendingWithdrawal: StateFlow<Boolean> = _hasPendingWithdrawal

    private val db =
        FirebaseDatabase.getInstance("https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/")
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchTotalSavings()
    }

    private fun fetchTotalSavings() {
        val userId = auth.currentUser?.uid ?: return
        val profileRef = db.reference.child("smartSaveProfile").child(userId)

        profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("totalSaved").getValue(Double::class.java)
                _totalSavings.value = value

                val hasPending = snapshot.child("transactions").children.any {
                    it.child("type").getValue(String::class.java) == "PENDING_WITHDRAWAL"
                }
                _hasPendingWithdrawal.value = hasPending
            }

            override fun onCancelled(error: DatabaseError) {
                _totalSavings.value = null
                _hasPendingWithdrawal.value = false
            }
        })
    }

    fun submitWithdrawal(amount: Double, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)
        val profileRef = db.reference.child("smartSaveProfile").child(userId)

        profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalSaved = snapshot.child("totalSaved").getValue(Double::class.java) ?: 0.0

                val pendingSum = snapshot.child("transactions").children
                    .filter {
                        it.child("type").getValue(String::class.java) == "PENDING_WITHDRAWAL"
                    }
                    .sumOf { it.child("amount").getValue(Double::class.java) ?: 0.0 }

                val availableBalance = totalSaved + pendingSum

                if (availableBalance < amount) {
                    onComplete(false)
                    return
                }

                val txTime = System.currentTimeMillis()
                val txRef = profileRef.child("transactions").child(txTime.toString())
                val txData = mapOf(
                    "amount" to -amount,
                    "currency" to "BGN",
                    "type" to "PENDING_WITHDRAWAL",
                    "description" to "Scheduled Withdrawal",
                    "date" to txTime,
                    "timestamp" to txTime
                )

                profileRef.child("totalSaved")
                    .setValue(totalSaved - amount)
                    .addOnSuccessListener {
                        txRef.setValue(txData).addOnSuccessListener {
                            onComplete(true)
                            fetchTotalSavings()
                        }.addOnFailureListener {
                            onComplete(false)
                        }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false)
            }
        })
    }

    private fun scheduleCompletion(txRef: DatabaseReference) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            txRef.child("type").setValue("WITHDRAWAL")
        }, 24 * 60 * 60 * 1000)
    }
}

@Composable
fun WithdrawScreen(navController: NavController, viewModel: WithdrawViewModel = viewModel()) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var amount by remember { mutableStateOf(TextFieldValue("")) }
    val totalSavings by viewModel.totalSavings.collectAsState()
    val hasPending by viewModel.hasPendingWithdrawal.collectAsState()
    val displayAmount = totalSavings?.let { String.format("%.2f BGN", it) } ?: "Loading..."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_circle_left_24),
                    contentDescription = "Return",
                    tint = blue
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "WITHDRAW SAVINGS",
            fontSize = 20.sp,
            color = blue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(160.dp)
                .border(width = 4.dp, color = blue, shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    displayAmount,
                    fontSize = 22.sp, // Match dashboard
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    "Total Savings",
                    style = typography.bodySmall.copy(fontSize = 14.sp),
                    color = colors.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (hasPending) {
            Text(
                "Withdrawal in processing",
                style = typography.bodySmall.copy(fontSize = 13.sp),
                color = colors.error,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Enter Amount",
            style = typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = colors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(greyFieldBackground, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, colors.outline, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "BGN",
                    style = typography.bodyLarge.copy(fontSize = 16.sp),
                    color = colors.onBackground
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                singleLine = true,
                placeholder = {
                    Text("0.00", color = colors.onBackground.copy(alpha = 0.4f))
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(56.dp),
                textStyle = typography.bodyLarge.copy(fontSize = 18.sp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = colors.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The money will leave the account in 24 hours",
                style = typography.bodySmall.copy(fontSize = 14.sp),
                color = colors.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val value = amount.text.toDoubleOrNull()
                if (value == null || value <= 0.0) {
                    Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT)
                        .show()
                    return@Button
                }

                viewModel.submitWithdrawal(value) { success ->
                    if (success) {
                        Toast.makeText(
                            context,
                            "You have withdrawn %.2f BGN".format(value),
                            Toast.LENGTH_LONG
                        ).show()

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Withdraw.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }, 1600) // long enough for the toast to be seen
                    } else {
                        Toast.makeText(context, "Not enough available balance.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = blue)
        ) {
            Text(
                text = "Withdraw",
                style = typography.labelLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}