package com.example.smartsave.util;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query; // Import Query
import com.google.firebase.database.ValueEventListener;
import com.example.smartsave.model.Transaction;

import java.util.Calendar; // For date calculations
import java.util.TimeZone; // For UTC Calendar

public class SavingsCalculator {

    private static final String TAG = "SavingsCalculator";
    private static final String SMART_SAVE_PROFILE_NODE = "smartSaveProfile";
    private static final String TRANSACTIONS_SUB_NODE = "transactions";
    private static final String DB_URL = "https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/";

    // Callback for total saved calculation
    public interface CalculationCallback {
        void onSuccess(double newTotalSaved);
        void onError(String errorMessage);
    }

    // Callback for interest earned calculation
    public interface InterestCalculationCallback {
        void onSuccess(double totalInterestEarned, String currency); // Pass currency too
        void onError(String errorMessage);
    }

    // Callback for progress this month
    public interface MonthlyProgressCallback {
        void onSuccess(double totalProgress, String currency);
        void onError(String errorMessage);
    }


    public static void recalculateAndUpdatetotalSaved(CalculationCallback callback) {
        // ... (your existing recalculateAndUpdatetotalSaved method - no changes here) ...
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in. Cannot recalculate savings.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        DatabaseReference userProfileTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        userProfileTransactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "No transactions found for user " + userId + ". Setting totalSaved to 0.");
                    updateTotalSavedInProfile(userId, 0.0, callback);
                    return;
                }

                double newTotalSaved = 0.0;
                Log.d(TAG, "--- Recalculating totalSaved for user: " + userId + " ---");
                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        switch (type) {
                            case "INCOME":
                            case "EXPENSE": // Assuming EXPENSE also contributes via savingsCalculated
                                //newTotalSaved += transaction.getSavingsCalculated();
                                //Log.d(TAG, "    " + type + ": Added savingsCalculated " + transaction.getSavingsCalculated() + ". Current Sum: " + newTotalSaved);
                                break;
                            case "WITHDRAW":
                                newTotalSaved -= transaction.getAmount();
                                Log.d(TAG, "    WITHDRAW: Subtracted amount " + transaction.getAmount() + ". Current Sum: " + newTotalSaved);
                                break;
                            case "SAVINGS_DEPOSIT":
                            case "INTEREST_PAYMENT":
                                newTotalSaved += transaction.getAmount();
                                Log.d(TAG, "    " + type + ": Added amount " + transaction.getAmount() + ". Current Sum: " + newTotalSaved);
                                break;
                            default:
                                Log.d(TAG, "    UNKNOWN/SKIPPED Type: '" + type + "'. Sum remains: " + newTotalSaved);
                                break;
                        }
                    }
                }
                Log.i(TAG, "--- FINAL Recalculated totalSaved for user " + userId + ": " + newTotalSaved + " ---");
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
        // ... (your existing updateTotalSavedInProfile method - no changes here) ...
        DatabaseReference totalSavedFieldRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child("totalSaved");

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


    // --- METHOD to calculate interest earned in the last rolling month ---
    public static void calculateInterestEarnedLastMonth(InterestCalculationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcInterest] User not logged in."); // Added prefix for clarity
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999);
        long endTimestamp = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        Log.i(TAG, "[CalcInterest] User: " + userId);
        Log.i(TAG, "[CalcInterest] Start Timestamp: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")"); // Use java.util.Date
        Log.i(TAG, "[CalcInterest] End Timestamp:   " + endTimestamp   + " (" + new java.util.Date(endTimestamp) + ")");

        DatabaseReference userTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        Query interestQuery = userTransactionsRef.orderByChild("timestamp")
                .startAt((double) startTimestamp)
                .endAt((double) endTimestamp);

        Log.d(TAG, "[CalcInterest] Query Path: " + interestQuery.getRef().toString());

        interestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalInterest = 0.0;
                String currency = "EUR";
                boolean currencyFound = false;

                Log.i(TAG, "[CalcInterest] onDataChange. Snapshot exists: " + dataSnapshot.exists() + ", Children: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "[CalcInterest] No tx in date range for user " + userId);
                    if (callback != null) callback.onSuccess(0.0, currency);
                    return;
                }

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
                    Log.d(TAG, "[CalcInterest]   Processing Tx ID: " + txSnapshot.getKey() +
                            ", Type: " + (transaction != null ? transaction.getType() : "N/A") +
                            ", Amount: " + (transaction != null ? transaction.getAmount() : "N/A") +
                            ", Timestamp: " + (transaction != null ? transaction.getTimestamp() : "N/A") +
                            " (" + (transaction != null && transaction.getTimestamp() > 0 ? new java.util.Date(transaction.getTimestamp()) : "N/A") + ")");

                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        if ("INTEREST_PAYMENT".equals(type)) {
                            totalInterest += transaction.getAmount();
                            Log.d(TAG, "[CalcInterest]     +++ INTEREST_PAYMENT: Added " + transaction.getAmount() + ". Sum: " + totalInterest);
                            if (!currencyFound && transaction.getCurrency() != null && !transaction.getCurrency().isEmpty()) {
                                currency = transaction.getCurrency();
                                currencyFound = true;
                            }
                        } else {
                            Log.d(TAG, "[CalcInterest]     --- Not INTEREST_PAYMENT. Actual type: '" + type + "'");
                        }
                    }
                }
                Log.i(TAG, "[CalcInterest] FINAL Total interest for user " + userId + ": " + totalInterest + " " + currency);
                if (callback != null) callback.onSuccess(totalInterest, currency);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read transactions for monthly interest: " + databaseError.getMessage());
                if (callback != null) callback.onError("Failed to read transactions for monthly interest: " + databaseError.getMessage());
            }
        });
    }


    // --- METHOD to calculate "Progress this month" (Income savings + Interest) ---
    public static void calculateProgressThisMonth(MonthlyProgressCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcProgress] User not logged in.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        // 1. Determine Date Range (last 30 days for a rolling month)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999);
        long endTimestamp = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        Log.i(TAG, "[CalcProgress] User: " + userId);
        Log.i(TAG, "[CalcProgress] Period Start: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")");
        Log.i(TAG, "[CalcProgress] Period End:   " + endTimestamp   + " (" + new java.util.Date(endTimestamp) + ")");

        // 2. Reference transactions and Query
        DatabaseReference userTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        Query progressQuery = userTransactionsRef.orderByChild("timestamp")
                .startAt((double) startTimestamp)
                .endAt((double) endTimestamp);

        Log.d(TAG, "[CalcProgress] Query Path: " + progressQuery.getRef().toString());

        progressQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalProgress = 0.0;
                String currency = "EUR"; // Default
                boolean currencyFound = false;

                Log.i(TAG, "[CalcProgress] onDataChange. Snapshot exists: " + dataSnapshot.exists() + ", Children in range: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "[CalcProgress] No tx in date range for user " + userId);
                    if (callback != null) callback.onSuccess(0.0, currency);
                    return;
                }

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
//                    Log.d(TAG, "[CalcProgress]   Processing Tx ID: " + txSnapshot.getKey() +
//                            ", Type: " + (transaction != null ? transaction.getType() : "N/A") +
//                            ", Amount: " + (transaction != null ? transaction.getAmount() : "N/A") +
//                            ", SavingsCalculated: " + (transaction != null ? transaction.getSavingsCalculated() : "N/A") +
//                            ", Timestamp: " + (transaction != null ? transaction.getTimestamp() : "N/A"));

                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        switch (type) {
                            case "INCOME":
                                //totalProgress += transaction.getSavingsCalculated();
                                //Log.d(TAG, "[CalcProgress]     +++ INCOME: Added savingsCalculated " + transaction.getSavingsCalculated() + ". Current Progress: " + totalProgress);
                                break;
                            case "INTEREST_PAYMENT":
                                totalProgress += transaction.getAmount();
                                Log.d(TAG, "[CalcProgress]     +++ INTEREST_PAYMENT: Added amount " + transaction.getAmount() + ". Current Progress: " + totalProgress);
                                break;
                            default:
                                Log.d(TAG, "[CalcProgress]     --- Type '" + type + "' not included in progress. Skipping.");
                                break; // Other types like WITHDRAW, EXPENSE (if not contributing to savings) are ignored
                        }

                        // Capture currency from the first relevant transaction
                        if (!currencyFound && (type.equals("INCOME") || type.equals("INTEREST_PAYMENT"))) {
                            if (transaction.getCurrency() != null && !transaction.getCurrency().isEmpty()) {
                                currency = transaction.getCurrency();
                                currencyFound = true;
                            }
                        }
                    }
                }
                Log.i(TAG, "[CalcProgress] FINAL Progress this month for user " + userId + ": " + totalProgress + " " + currency);
                if (callback != null) callback.onSuccess(totalProgress, currency);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "[CalcProgress] onCancelled: " + databaseError.getMessage(), databaseError.toException());
                if (callback != null) callback.onError("Failed to read transactions for monthly progress: " + databaseError.getMessage());
            }
        });
    }
// --- END METHOD for "Progress this month" ---

}