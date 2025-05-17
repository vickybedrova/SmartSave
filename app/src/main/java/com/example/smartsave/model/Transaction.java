package com.example.smartsave.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Transaction {
    private String id;

    private String description;
    private double amount;
    private String type;
    private double savingsCalculated;
    private long timestamp;
    private String currency;


    public Transaction() {
    }

    public Transaction(String description, double amount, String type, double savingsCalculated, long timestamp, String currency) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.savingsCalculated = savingsCalculated;
        this.timestamp = timestamp;
        this.currency = currency;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public double getSavingsCalculated() { return savingsCalculated; }
    public long getTimestamp() { return timestamp; }
    public String getCurrency() { return currency == null ? "EUR" : currency; } // Default currency if null

    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }
    public void setSavingsCalculated(double savingsCalculated) { this.savingsCalculated = savingsCalculated; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCurrency(String currency) { this.currency = currency; }

    @Exclude
    public String getFormattedDate(String format) {
        if (timestamp == 0L) return "Processing";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Exclude
    public String getFormattedDate() { // Overload with default format
        return getFormattedDate("dd MMM yyyy, HH:mm");
    }

    @Exclude
    public String getSavingsImpactForList() {
        switch (type != null ? type.toUpperCase() : "") {
            case "INCOME":
            case "EXPENSE":
                if (savingsCalculated > 0) {
                    return String.format(Locale.US, "+ %.2f %s", savingsCalculated, getCurrency());
                } else if (savingsCalculated < 0) {
                    return String.format(Locale.US, "%.2f %s", savingsCalculated, getCurrency());
                }
                return "";
            case "WITHDRAW":
                return String.format(Locale.US, "- %.2f %s", amount, getCurrency());
            case "SAVINGS_DEPOSIT":
            case "INTEREST_PAYMENT":
                return String.format(Locale.US, "+ %.2f %s", amount, getCurrency());
            default:
                return "";
        }
    }

    @Exclude
    public String getDisplayAmountForList() {
        String typeUpper = (type != null) ? type.toUpperCase() : "";

        switch (typeUpper) {
            case "INCOME":
            case "EXPENSE":
                return String.format(Locale.US, "Amount: %.2f %s", amount, getCurrency());
            case "WITHDRAW":
            case "SAVINGS_DEPOSIT":
            case "INTEREST_PAYMENT":
                return "";
            default:
                if (amount != 0) {
                    return String.format(Locale.US, "Amount: %.2f %s", amount, getCurrency());
                }
                return "";
        }
    }

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