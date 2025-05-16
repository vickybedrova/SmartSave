package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class AuthSessionRequest(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)