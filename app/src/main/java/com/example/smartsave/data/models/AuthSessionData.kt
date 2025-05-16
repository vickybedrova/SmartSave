package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class AuthSessionData(
    @SerializedName("session") val session: String?,
    @SerializedName("expires_in") val expiresIn: Int?
)