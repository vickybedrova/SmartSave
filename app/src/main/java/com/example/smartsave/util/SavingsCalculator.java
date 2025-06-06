package com.example.smartsave.util;

import android.icu.text.SimpleDateFormat;
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

import java.util.ArrayList;
import java.util.Calendar; // For date calculations
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone; // For UTC Calendar

public class SavingsCalculator {

    private static final String TAG = "SavingsCalculator";
    private static final String SMART_SAVE_PROFILE_NODE = "smartSaveProfile";
    private static final String TRANSACTIONS_SUB_NODE = "transactions";
    private static final String DB_URL = "https://smartsave-e0e7b-default-rtdb.europe-west1.firebasedatabase.app/";

    public interface CompoundInterestProjectionCallback {
        void onSuccess(double futureValue, double interestEarned);

        void onError(String errorMessage);
    }

    public interface CalculationCallback {
        void onSuccess(double newTotalSaved);

        void onError(String errorMessage);
    }

    public interface InterestCalculationCallback {
        void onSuccess(double totalInterestEarned, String currency);

        void onError(String errorMessage);
    }

    public interface MonthlyProgressCallback {
        void onSuccess(double totalProgress, String currency);

        void onError(String errorMessage);
    }


    public interface SelectedMonthInterestCallback {
        void onSuccess(double totalInterestForMonth, String currency);

        void onError(String errorMessage);
    }

    public interface SelectedMonthIncomeSavingsCallback {
        void onSuccess(double totalIncomeSavingsForMonth, String currency);

        void onError(String errorMessage);
    }

    public interface MonthlySavingsGrowthCallback {
        void onSuccess(List<Map<String, Object>> monthlyData);

        void onError(String errorMessage);
    }


    public static void calculateMonthlySavingsGrowth(
            int targetYear,
            int targetMonth,
            int numberOfMonths,
            MonthlySavingsGrowthCallback callback
    ) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcGrowth] User not logged in.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        if (numberOfMonths <= 0) {
            Log.e(TAG, "[CalcGrowth] Number of months must be positive.");
            if (callback != null) callback.onError("Invalid number of months.");
            return;
        }
        if (targetMonth < 1 || targetMonth > 12) {
            Log.e(TAG, "[CalcGrowth] Invalid targetMonth: " + targetMonth);
            if (callback != null) callback.onError("Invalid target month.");
            return;
        }


        DatabaseReference userTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        Calendar endOfOverallPeriodCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endOfOverallPeriodCalendar.set(targetYear, targetMonth - 1, 1); // Start of target month (month is 0-indexed)
        endOfOverallPeriodCalendar.set(Calendar.DAY_OF_MONTH, endOfOverallPeriodCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Last day of target month
        endOfOverallPeriodCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endOfOverallPeriodCalendar.set(Calendar.MINUTE, 59);
        endOfOverallPeriodCalendar.set(Calendar.SECOND, 59);
        endOfOverallPeriodCalendar.set(Calendar.MILLISECOND, 999);
        long overallEndTimestamp = endOfOverallPeriodCalendar.getTimeInMillis();

        Log.i(TAG, "[CalcGrowth] Calculating growth for " + numberOfMonths + " months, ending " + targetMonth + "/" + targetYear);
        Log.i(TAG, "[CalcGrowth] Fetching all transactions up to: " + new java.util.Date(overallEndTimestamp));

        Query allRelevantTransactionsQuery = userTransactionsRef.orderByChild("timestamp")
                .endAt((double) overallEndTimestamp); // Get all tx up to end of the period

        allRelevantTransactionsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Transaction> allFetchedTransactions = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                        Transaction transaction = txSnapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            allFetchedTransactions.add(transaction);
                        }
                    }
                    // Sort by timestamp to ensure correct cumulative calculation
                    Collections.sort(allFetchedTransactions, (t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));
                } else {
                    Log.i(TAG, "[CalcGrowth] No transactions found for user " + userId + " up to the period end.");
                }


                List<Map<String, Object>> monthlyGrowthData = new ArrayList<>();
                Calendar monthIteratorCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                // Start iterating from the target month and go backwards
                monthIteratorCalendar.set(targetYear, targetMonth - 1, 1); // month is 0-indexed

                SimpleDateFormat monthNameFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);

                for (int i = 0; i < numberOfMonths; i++) {
                    // Determine the end of the current month we are calculating for
                    Calendar currentMonthEndCalendar = (Calendar) monthIteratorCalendar.clone();
                    currentMonthEndCalendar.set(Calendar.DAY_OF_MONTH, currentMonthEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    currentMonthEndCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    currentMonthEndCalendar.set(Calendar.MINUTE, 59);
                    currentMonthEndCalendar.set(Calendar.SECOND, 59);
                    currentMonthEndCalendar.set(Calendar.MILLISECOND, 999);
                    long currentMonthEndTimestamp = currentMonthEndCalendar.getTimeInMillis();

                    double totalSavedUpToThisMonthEnd = 0.0;
                    for (Transaction transaction : allFetchedTransactions) {
                        // Only consider transactions up to the end of *this specific month* in the iteration
                        if (transaction.getTimestamp() <= currentMonthEndTimestamp) {
                            String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                            switch (type) {
                                case "INCOME":
                                case "EXPENSE":
                                    totalSavedUpToThisMonthEnd += transaction.getSavingsCalculated();
                                    break;
                                case "WITHDRAW":
                                    totalSavedUpToThisMonthEnd -= transaction.getAmount();
                                    break;
                                case "SAVINGS_DEPOSIT":
                                case "INTEREST_PAYMENT":
                                    totalSavedUpToThisMonthEnd += transaction.getAmount();
                                    break;
                            }
                        }
                    }

                    Map<String, Object> monthPoint = new HashMap<>();
                    monthPoint.put("monthName", monthNameFormat.format(monthIteratorCalendar.getTime()));
                    monthPoint.put("year", monthIteratorCalendar.get(Calendar.YEAR));
                    monthPoint.put("savings", totalSavedUpToThisMonthEnd);
                    monthlyGrowthData.add(monthPoint);

                    Log.d(TAG, "[CalcGrowth] End of " + (monthIteratorCalendar.get(Calendar.MONTH) + 1) + "/" + monthIteratorCalendar.get(Calendar.YEAR) +
                            ": Total Saved = " + totalSavedUpToThisMonthEnd);

                    monthIteratorCalendar.add(Calendar.MONTH, -1);
                }

                Collections.reverse(monthlyGrowthData);
                if (callback != null) callback.onSuccess(monthlyGrowthData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "[CalcGrowth] Failed to read transactions: " + databaseError.getMessage());
                if (callback != null)
                    callback.onError("Failed to read transactions for growth chart: " + databaseError.getMessage());
            }
        });
    }


    public static void calculateCompoundInterestProjection(
            double currentTotalSavings,
            double annualInterestRate,
            CompoundInterestProjectionCallback callback
    ) {
        if (currentTotalSavings < 0) {
            if (callback != null) callback.onError("No current savings to project.");
            return;
        }
        if (annualInterestRate < 0) {
            if (callback != null) callback.onError("Invalid interest rate.");
            return;
        }

        double rateForSixMonths = annualInterestRate / 2.0;
        double interestEarned = currentTotalSavings * rateForSixMonths;
        double futureValue = currentTotalSavings + interestEarned;

        Log.i(TAG, "[CompoundProjection] Current: " + currentTotalSavings +
                ", Rate (annual): " + (annualInterestRate * 100) + "%" +
                ", Future Value (6m): " + futureValue +
                ", Interest Earned (6m): " + interestEarned);

        if (callback != null) {
            callback.onSuccess(futureValue, interestEarned);
        }
    }


    public static void calculateIncomeSavingsForSelectedMonth(
            int year,
            int month,
            SelectedMonthIncomeSavingsCallback callback
    ) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcSelectedMonthIncomeSavings] User not logged in.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        if (month < 1 || month > 12) {
            Log.e(TAG, "[CalcSelectedMonthIncomeSavings] Invalid month provided: " + month);
            if (callback != null) callback.onError("Invalid month selected.");
            return;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.set(year, month - 1, 1, 0, 0, 0); // month is 0-indexed
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        calendar.set(year, month - 1, 1); // Reset to first day
        calendar.add(Calendar.MONTH, 1);    // Go to first day of next month
        calendar.add(Calendar.MILLISECOND, -1); // Go to last millisecond of selected month
        long endTimestamp = calendar.getTimeInMillis();

        Log.i(TAG, "[CalcSelectedMonthIncomeSavings] User: " + userId + ", Year: " + year + ", Month: " + month);
        Log.i(TAG, "[CalcSelectedMonthIncomeSavings] Period Start: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")");
        Log.i(TAG, "[CalcSelectedMonthIncomeSavings] Period End:   " + endTimestamp + " (" + new java.util.Date(endTimestamp) + ")");

        DatabaseReference userTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        Query incomeSavingsQuery = userTransactionsRef.orderByChild("timestamp")
                .startAt((double) startTimestamp)
                .endAt((double) endTimestamp);

        Log.d(TAG, "[CalcSelectedMonthIncomeSavings] Query Path: " + incomeSavingsQuery.getRef().toString());

        incomeSavingsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalIncomeSavings = 0.0;
                String currency = "BGN"; // Default
                boolean currencyFound = false;

                Log.i(TAG, "[CalcSelectedMonthIncomeSavings] onDataChange. Snapshot exists: " + dataSnapshot.exists() + ", Children in range: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    Log.i(TAG, "[CalcSelectedMonthIncomeSavings] No transactions found in the selected month/year for user " + userId);
                    if (callback != null) callback.onSuccess(0.0, currency);
                    return;
                }

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        if ("INCOME".equals(type)) {
                            // Ensure transaction is within the exact month
                            if (transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                                totalIncomeSavings += transaction.getSavingsCalculated();
                                Log.d(TAG, "[CalcSelectedMonthIncomeSavings]     +++ INCOME: Added savingsCalculated " + transaction.getSavingsCalculated() + ". Sum: " + totalIncomeSavings);
                                if (!currencyFound && transaction.getCurrency() != null && !transaction.getCurrency().isEmpty()) {
                                    currency = transaction.getCurrency();
                                    currencyFound = true;
                                }
                            }
                        }
                    }
                }
                Log.i(TAG, "[CalcSelectedMonthIncomeSavings] FINAL Total income savings for " + month + "/" + year + " for user " + userId + ": " + totalIncomeSavings + " " + currency);
                if (callback != null) callback.onSuccess(totalIncomeSavings, currency);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "[CalcSelectedMonthIncomeSavings] onCancelled: " + databaseError.getMessage(), databaseError.toException());
                if (callback != null)
                    callback.onError("Failed to read transactions for selected month's income savings: " + databaseError.getMessage());
            }
        });
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
                            case "EXPENSE":
                                newTotalSaved += transaction.getSavingsCalculated();
                                Log.d(TAG, "    " + type + ": Added savingsCalculated " + transaction.getSavingsCalculated() + ". Current Sum: " + newTotalSaved);
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
                if (callback != null)
                    callback.onError("Failed to read transactions: " + databaseError.getMessage());
            }
        });
    }


    private static void updateTotalSavedInProfile(String userId, double newTotal, CalculationCallback callback) {
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
                    if (callback != null)
                        callback.onError("Failed to update totalSaved: " + e.getMessage());
                });
    }

    public static void calculateInterestEarnedLastMonth(InterestCalculationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcInterest] User not logged in."); // Added prefix for clarity
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTimestamp = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        Log.i(TAG, "[CalcInterest] User: " + userId);
        Log.i(TAG, "[CalcInterest] Start Timestamp: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")"); // Use java.util.Date
        Log.i(TAG, "[CalcInterest] End Timestamp:   " + endTimestamp + " (" + new java.util.Date(endTimestamp) + ")");

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
                String currency = "BGN";
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
                if (callback != null)
                    callback.onError("Failed to read transactions for monthly interest: " + databaseError.getMessage());
            }
        });
    }


    public static void calculateProgressThisMonth(MonthlyProgressCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcProgress] User not logged in.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTimestamp = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        Log.i(TAG, "[CalcProgress] User: " + userId);
        Log.i(TAG, "[CalcProgress] Period Start: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")");
        Log.i(TAG, "[CalcProgress] Period End:   " + endTimestamp + " (" + new java.util.Date(endTimestamp) + ")");

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
                String currency = "BGN"; // Default
                boolean currencyFound = false;

                Log.i(TAG, "[CalcProgress] onDataChange. Snapshot exists: " + dataSnapshot.exists() + ", Children in range: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "[CalcProgress] No tx in date range for user " + userId);
                    if (callback != null) callback.onSuccess(0.0, currency);
                    return;
                }

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);
                    Log.d(TAG, "[CalcProgress]   Processing Tx ID: " + txSnapshot.getKey() +
                            ", Type: " + (transaction != null ? transaction.getType() : "N/A") +
                            ", Amount: " + (transaction != null ? transaction.getAmount() : "N/A") +
                            ", SavingsCalculated: " + (transaction != null ? transaction.getSavingsCalculated() : "N/A") +
                            ", Timestamp: " + (transaction != null ? transaction.getTimestamp() : "N/A"));

                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        switch (type) {
                            case "INCOME":
                                totalProgress += transaction.getSavingsCalculated();
                                Log.d(TAG, "[CalcProgress]     +++ INCOME: Added savingsCalculated " + transaction.getSavingsCalculated() + ". Current Progress: " + totalProgress);
                                break;
                            case "INTEREST_PAYMENT":
                                totalProgress += transaction.getAmount();
                                Log.d(TAG, "[CalcProgress]     +++ INTEREST_PAYMENT: Added amount " + transaction.getAmount() + ". Current Progress: " + totalProgress);
                                break;
                            default:
                                Log.d(TAG, "[CalcProgress]     --- Type '" + type + "' not included in progress. Skipping.");
                                break; // Other types like WITHDRAW, EXPENSE (if not contributing to savings) are ignored
                        }

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
                if (callback != null)
                    callback.onError("Failed to read transactions for monthly progress: " + databaseError.getMessage());
            }
        });
    }

    public static void calculateInterestForSelectedMonth(
            int year,
            int month, // 1 for January, 2 for February, etc.
            SelectedMonthInterestCallback callback
    ) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[CalcSelectedMonthInterest] User not logged in.");
            if (callback != null) callback.onError("User not logged in.");
            return;
        }
        String userId = currentUser.getUid();

        if (month < 1 || month > 12) {
            Log.e(TAG, "[CalcSelectedMonthInterest] Invalid month provided: " + month);
            if (callback != null) callback.onError("Invalid month selected.");
            return;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.set(year, month - 1, 1, 0, 0, 0); // month is 0-indexed in Calendar
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();


        calendar.set(year, month - 1, 1); // Reset to first day of current month
        calendar.add(Calendar.MONTH, 1);    // Go to first day of next month
        calendar.add(Calendar.MILLISECOND, -1); // Go to last millisecond of selected month
        long endTimestamp = calendar.getTimeInMillis();


        Log.i(TAG, "[CalcSelectedMonthInterest] User: " + userId + ", Year: " + year + ", Month: " + month);
        Log.i(TAG, "[CalcSelectedMonthInterest] Period Start: " + startTimestamp + " (" + new java.util.Date(startTimestamp) + ")");
        Log.i(TAG, "[CalcSelectedMonthInterest] Period End:   " + endTimestamp + " (" + new java.util.Date(endTimestamp) + ")");

        DatabaseReference userTransactionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference(SMART_SAVE_PROFILE_NODE)
                .child(userId)
                .child(TRANSACTIONS_SUB_NODE);

        Query interestQuery = userTransactionsRef.orderByChild("timestamp")
                .startAt((double) startTimestamp)
                .endAt((double) endTimestamp);

        Log.d(TAG, "[CalcSelectedMonthInterest] Query Path: " + interestQuery.getRef().toString());

        interestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalInterest = 0.0;
                String currency = "BGN";
                boolean currencyFound = false;

                Log.i(TAG, "[CalcSelectedMonthInterest] onDataChange. Snapshot exists: " + dataSnapshot.exists() + ", Children in range: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    Log.i(TAG, "[CalcSelectedMonthInterest] No transactions found in the selected month/year for user " + userId);
                    if (callback != null)
                        callback.onSuccess(0.0, currency); // Return 0 if no transactions
                    return;
                }

                for (DataSnapshot txSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = txSnapshot.getValue(Transaction.class);

                    if (transaction != null) {
                        String type = transaction.getType() != null ? transaction.getType().toUpperCase() : "";
                        if ("INTEREST_PAYMENT".equals(type)) {
                            // Ensure transaction is within the exact month (Firebase query is inclusive)
                            if (transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                                totalInterest += transaction.getAmount();
                                Log.d(TAG, "[CalcSelectedMonthInterest]     +++ INTEREST_PAYMENT: Added " + transaction.getAmount() + ". Sum: " + totalInterest);
                                if (!currencyFound && transaction.getCurrency() != null && !transaction.getCurrency().isEmpty()) {
                                    currency = transaction.getCurrency();
                                    currencyFound = true;
                                }
                            } else {
                            }
                        }
                    }
                }
                Log.i(TAG, "[CalcSelectedMonthInterest] FINAL Total interest for " + month + "/" + year + " for user " + userId + ": " + totalInterest + " " + currency);
                if (callback != null) callback.onSuccess(totalInterest, currency);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "[CalcSelectedMonthInterest] onCancelled: " + databaseError.getMessage(), databaseError.toException());
                if (callback != null)
                    callback.onError("Failed to read transactions for selected month's interest: " + databaseError.getMessage());
            }
        });
    }


}