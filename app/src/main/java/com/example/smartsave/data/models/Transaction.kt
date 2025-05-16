package com.example.smartsave.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Transaction(
    val id: String,             // UI-friendly ID (usually String)
    val timestamp: Long,        // Epoch milliseconds (UTC) for easy sorting/filtering
    val amount: Double,         // The primary amount for display
    val currency: String,       // The currency of the primary amount
    val description: String,    // UI-friendly description
    val type: String? = null    // Optional: "Credit", "Debit", or transaction_type from API
    // Add any other fields your UI specifically needs, derived or mapped
) {
    fun getFormattedDate(): String {
        if (timestamp == 0L) return "Invalid Date"
        // Example: "27 Oct 2023, 14:30"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        // Assuming timestamp is already UTC, format it for display in the user's local timezone
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp))
    }

    fun getDisplayAmountForList(): String {
        // Example: "€123.45" or "$50.00"
        return String.format(Locale.getDefault(), "%.2f %s", amount, currency)
        // Consider using java.text.NumberFormat for locale-aware currency formatting
    }

    fun getSavingsImpactForList(): String {
        // This is specific to your SmartSave logic.
        // For example, if 'amount' is the saved portion:
        // if (amount > 0) return String.format(Locale.getDefault(), "+%.2f %s", amount, currency)
        // else if (amount < 0) return String.format(Locale.getDefault(), "%.2f %s", amount, currency) // Or handle negative savings
        // else return ""
        // For now, placeholder:
        return if (type == "Credit" && amount > 0) { // Simplified example: positive credit is savings
            String.format(Locale.getDefault(), "+%.2f %s", amount, currency)
        } else {
            "" // No savings impact or debit
        }
    }
    fun getSavingsCalculated(){

    }
}