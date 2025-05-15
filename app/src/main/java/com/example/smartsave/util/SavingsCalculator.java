package com.example.smartsave.util; // Or your preferred package for utilities

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.smartsave.model.Transaction; // Your Transaction model

import java.util.Map;
import java.util.HashMap; // If you need to iterate over a map directly

public class SavingsCalculator {

    private static final String TAG = "SavingsCalculator";
    // Node name for SmartSave profiles (as per your Firebase structure)
    private static final String SMART_SAVE_PROFILE_NODE = "smartSaveProfile";
    // Sub-node name for transactions within a profile
    private static final String TRANSACTIONS_SUB_NODE = "transactions";
    private static final String DB_URL = "https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/";


    public interface CalculationCallback {
        void onSuccess(double newTotalSaved);
        void onError(String errorMessage);
    }

    /**
     * Recalculates totalSaved based on nested transactions and updates it in the profile.
     */
    public static void recalculateAndUpdatetotalSaved(CalculationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in. Cannot recalculate savings.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        // Reference to the user's specific profile node, which contains the transactions
        DatabaseReference userProfileTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE); // Path to the nested transactions

        userProfileTransactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "No transactions found for user " + userId + ". Setting totalSaved to 0.");
                    updateTotalSavedInProfile(userId, 0.0, callback);
                    return;
                }

                double newTotalSaved = 0.0;
                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        switch (type) {
                            case "INCOME":
                                // Add savingsCalculated for INCOME
                                newTotalSaved += transaction.getSavingsCalculated();
                                break;
                            case "WITHDRAW":
                                // Subtract amount for WITHDRAW
                                newTotalSaved -= transaction.getAmount();
                                break;
                            // ... other cases if any
                        }
                    }
                }
                Log.i(TAG, "Recalculated totalSaved for user " + userId + ": " + newTotalSaved);
                updateTotalSavedInProfile(userId, newTotalSaved, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read transactions for recalculation: " + databaseError.getMessage());
                if (callback != null) callback.onError("Failed to read transactions: " + databaseError.getMessage());
            }
        });
    }

    private static void updateTotalSavedInProfile(String userId, double newTotal, CalculationCallback callback) {
        DatabaseReference totalSavedFieldRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child("totalSaved"); // Reference to the specific field to update

        totalSavedFieldRef.setValue(newTotal)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Successfully updated totalSaved for user " + userId + " to " + newTotal);
                    if (callback != null) callback.onSuccess(newTotal);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update totalSaved for user " + userId + ": " + e.getMessage());
                    if (callback != null) callback.onError("Failed to update totalSaved: " + e.getMessage());
                });
    }
}