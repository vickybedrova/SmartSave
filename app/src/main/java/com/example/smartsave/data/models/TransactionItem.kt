package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName // Good practice for GSON

data class TransactionItem(
    @SerializedName("id") // Matches JSON key
    val id: Long,

    @SerializedName("date")
    val date: String, // e.g., "2019-08-24T14:15:22Z"

    @SerializedName("payment_reference")
    val paymentReference: String?, // Make nullable if it can be missing

    @SerializedName("transaction_type")
    val transactionType: String?,

    @SerializedName("transaction_currency")
    val transactionCurrency: String,

    @SerializedName("transaction_amount")
    val transactionAmount: Double,

    @SerializedName("original_currency")
    val originalCurrency: String?,

    @SerializedName("original_amount")
    val originalAmount: Double?,

    @SerializedName("sign")
    val sign: String?, // e.g., "Credit", "Debit"

    @SerializedName("reference_number")
    val referenceNumber: String?,

    @SerializedName("reference_number_type")
    val referenceNumberType: String?,

    @SerializedName("terminal_id")
    val terminalId: String?,

    @SerializedName("serial_number")
    val serialNumber: String?,

    @SerializedName("account_number")
    val accountNumber: String?,

    @SerializedName("ruid")
    val ruid: String?,

    @SerializedName("billing_descriptor")
    val billingDescriptor: String?,

    @SerializedName("pan")
    val pan: String?,

    @SerializedName("description")
    val description: String
)