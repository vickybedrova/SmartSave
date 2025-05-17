package com.example.smartsave.data.api

import com.example.smartsave.data.models.AuthSessionData
import com.example.smartsave.data.models.OAuthTokenResponse // We'll create this
import com.example.smartsave.data.models.TransactionResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

data class AuthSessionRequest(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)

interface MyPosApiService {

    @GET("accounting/v1/transaction/list")
    @Headers(
        "Accept: application/json",
        "X-Application-Id: mps-app-30000837", // Your Partner App ID
        "X-Partner-Id: mps-p-10000107"      // Your Partner ID
    )
    fun getTransactions(
        @Header("X-Session") session: String,
        @Header("Authorization") authorization: String, // Should be "Bearer <token>"
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Call<TransactionResponse>

    @FormUrlEncoded
    @POST("api/v1/oauth/token") // Standard OAuth token endpoint
    @Headers(
        "Accept: application/json",
        "X-Application-Id: mps-app-30000837", // Your Partner App ID
        "X-Partner-Id: mps-p-10000107"      // Your Partner ID
    )
    fun getOAuthToken(
        @Field("grant_type") grantType: String = "password", // Typically "password" for this flow
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("username") username: String, // User's email/username
        @Field("password") password: String  // User's password
        // @Field("scope") scope: String? = null // Optional: if your API requires specific scopes
    ): Call<OAuthTokenResponse>

    @FormUrlEncoded
    @POST("api/v1/oauth/token")
    @Headers(
        "Accept: application/json",
        "X-Application-Id: mps-app-30000837",
        "X-Partner-Id: mps-p-10000107"
    )
    fun getClientCredentialsToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String, // e.g., client_9bb109631eca436baf4b91e40fe7caf0
        @Field("client_secret") clientSecret: String // e.g., secret_cc5e3ca77a56...
        // NO username, NO password
    ): Call<OAuthTokenResponse> // The response might be similar but for the application

    @POST("api/v1/auth/session") // Assuming it's a POST
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json", // <<< Specify JSON content type
        "X-Application-Id: mps-app-30000837",
        "X-Partner-Id: mps-p-10000107"
    )
    fun getAuthSession(
        @Body body: AuthSessionRequest
    ): Call<AuthSessionData> // Replace YourSessionResponseModel
}