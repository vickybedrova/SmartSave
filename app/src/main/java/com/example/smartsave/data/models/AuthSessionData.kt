package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class AuthSessionData( // Renamed from YourSessionResponseModel
    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("user_details")
    val userDetails: UserSessionDetails?, // Make nullable if it can be missing

    @SerializedName("expires_at")
    val expiresAt: String? // ISO 8601 date string
)

data class UserSessionDetails(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("display_name")
    val displayName: String
)