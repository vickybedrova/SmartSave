package com.example.smartsave.data.mappers // Or com.example.smartsave.data.models

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.smartsave.data.models.TransactionItem
import com.example.smartsave.model.Transaction as DomainTransaction // Alias for clarity
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

private const val TAG_MAPPER = "TransactionMapper"

@RequiresApi(Build.VERSION_CODES.O)
fun parseMyPosDateStringToEpochMillis(dateString: String?): Long {
    if (dateString.isNullOrBlank()) {
        Log.w(TAG_MAPPER, "Date string is null or blank, returning 0L")
        return 0L
    }
    return try {
        OffsetDateTime.parse(dateString).toInstant().toEpochMilli()
    } catch (e: DateTimeParseException) {
        Log.e(TAG_MAPPER, "Failed to parse date string: '$dateString'", e)
        0L // Or throw an exception / return a more specific error indicator
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun TransactionItem.toDomainModel(): DomainTransaction {
    return DomainTransaction(
        id = this.id.toString(),
        timestamp = parseMyPosDateStringToEpochMillis(this.date),
        amount = this.transactionAmount,
        currency = this.transactionCurrency,
        description = this.description.ifEmpty { "Transaction" }, // Fallback description
        type = this.sign ?: this.transactionType // Use 'sign' if available, else 'transactionType'
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun List<TransactionItem>.toDomainModelList(): List<DomainTransaction> {
    return this.map { it.toDomainModel() }
}