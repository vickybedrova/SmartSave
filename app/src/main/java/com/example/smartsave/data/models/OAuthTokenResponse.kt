package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class OAuthTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String, // Usually "Bearer"

    @SerializedName("expires_in")
    val expiresIn: Int, // Seconds until token expires

    @SerializedName("refresh_token")
    val refreshToken: String?, // Optional, for refreshing the access token

    @SerializedName("session_id") // Assuming myPOS returns a session_id like this
    val sessionId: String,

    // Add any other fields returned by the myPOS /oauth/token endpoint
    // For example, some APIs return a 'scope'
    @SerializedName("scope")
    val scope: String? = null
)