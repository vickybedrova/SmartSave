package com.example.smartsave.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@IgnoreExtraProperties // Important: Ignores fields in DB not in class
public class Transaction {

    private String id; // To store the Firebase push ID

    private String description;
    private double amount;
    private String type; // e.g., "INCOME", "WITHDRAW"
    private double savingsCalculated; // The part of 'amount' that went to savings (for INCOME/EXPENSE)
    private long timestamp; // Will be Long when retrieved from Firebase
    private String currency; // e.g., "EUR" - Assuming a default or fetched from profile

    // No-argument constructor required for Firebase deserialization
    public Transaction() {
    }

    // Optional: A constructor for manual creation if needed, though Firebase uses the no-arg one
    public Transaction(String description, double amount, String type, double savingsCalculated, long timestamp, String currency) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.savingsCalculated = savingsCalculated;
        this.timestamp = timestamp;
        this.currency = currency; // You might want a default like "EUR" or "BGN"
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public double getSavingsCalculated() { return savingsCalculated; }
    public long getTimestamp() { return timestamp; }
    public String getCurrency() { return currency == null ? "EUR" : currency; } // Default currency if null

    // --- Setters (Firebase needs these or public fields for deserialization) ---
    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }
    public void setSavingsCalculated(double savingsCalculated) { this.savingsCalculated = savingsCalculated; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCurrency(String currency) { this.currency = currency; }


    // --- Helper methods for UI display (Excluded from Firebase) ---

    @Exclude
    public String getFormattedDate(String format) {
        if (timestamp == 0L) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Exclude
    public String getFormattedDate() { // Overload with default format
        return getFormattedDate("dd MMM yyyy, HH:mm");
    }

    @Exclude
    public String getSavingsImpactForList() {
        // Based on your Firebase structure: "WITHDRAW" type directly affects savings with its 'amount'.
        // "INCOME" (and presumably "EXPENSE" if you add it) affects savings via 'savingsCalculated'.
        // Let's add "INTEREST_PAYMENT" as a type that positively impacts savings with its 'amount'.
        switch (type != null ? type.toUpperCase() : "") {
            case "INCOME":
            case "EXPENSE": // Assuming expenses also contribute a percentage to savings
                if (savingsCalculated > 0) {
                    return String.format(Locale.US, "+ %.2f %s", savingsCalculated, getCurrency());
                } else if (savingsCalculated < 0) { // Unlikely for 'savingsCalculated' but for completeness
                    return String.format(Locale.US, "%.2f %s", savingsCalculated, getCurrency());
                }
                return ""; // No savings impact shown if 0
            case "WITHDRAW": // Your existing type from DB
                return String.format(Locale.US, "- %.2f %s", amount, getCurrency());
            case "SAVINGS_DEPOSIT": // If you add manual deposits to savings
            case "INTEREST_PAYMENT": // For interest earned
                return String.format(Locale.US, "+ %.2f %s", amount, getCurrency());
            default:
                return ""; // Or some default for unknown types
        }
    }

    @Exclude
    public String getDisplayAmountForList() {
        // For INCOME/EXPENSE, show the gross amount.
        // For WITHDRAW/SAVINGS_DEPOSIT/INTEREST_PAYMENT, 'amount' is the direct savings impact.
        String prefix = "";
        if (Objects.equals(type, "INCOME") || Objects.equals(type, "EXPENSE" ) || Objects.equals(type, "WITHDRAW")) {
            prefix = "Amount: ";
        }
        return String.format(Locale.US, "%s%.2f %s", prefix, amount, getCurrency());
    }

    // Optional: toString, equals, hashCode for debugging or use in collections
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", savingsCalculated=" + savingsCalculated +
                ", timestamp=" + timestamp +
                ", currency='" + currency + '\'' +
                '}';
    }
}