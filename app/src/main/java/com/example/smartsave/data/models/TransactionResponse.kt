package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("items")
    val items: List<TransactionItem>,

    @SerializedName("page")
    val page: Int,

    @SerializedName("page_size")
    val pageSize: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("total_count")
    val totalCount: Int,

    @SerializedName("has_previous_page")
    val hasPreviousPage: Boolean,

    @SerializedName("has_next_page")
    val hasNextPage: Boolean
)