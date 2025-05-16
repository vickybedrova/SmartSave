package com.example.smartsave.data.models

import com.google.gson.annotations.SerializedName

data class OAuthTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("scope") val scope: String?
    // API doc mentions expires_on, add if you receive it and need it:
    // @SerializedName("expires_on") val expiresOn: Long?
)