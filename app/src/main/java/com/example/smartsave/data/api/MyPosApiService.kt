package com.example.smartsave.data.api

import com.example.smartsave.data.models.AuthSessionData
import com.example.smartsave.data.models.AuthSessionRequest // Import the moved model
import com.example.smartsave.data.models.OAuthTokenResponse
import com.example.smartsave.data.models.TransactionResponse
import retrofit2.Response // Import for suspend functions
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MyPosApiService {

    @GET("accounting/v1/transaction/list")
    @Headers(
        "Accept: application/json",
        "X-Application-Id: mps-app-30000837", // Your Partner App ID
        "X-Partner-Id: mps-p-10000107"      // Your Partner ID
    )
    suspend fun getTransactions( // Changed to suspend
        @Header("X-Session") session: String,
        @Header("Authorization") authorization: String, // Should be "Bearer <token>"
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<TransactionResponse> // Changed to Response<T>

    // STEP 1: Generate OAuth Token (Client Credentials)
    @FormUrlEncoded
    @POST("api/v1/oauth/token")
    @Headers(
        "Accept: application/json" // REMOVED X-Application-Id and X-Partner-Id
        // Content-Type: application/x-www-form-urlencoded is added by @FormUrlEncoded
    )
    suspend fun getClientCredentialsToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<OAuthTokenResponse>

    // STEP 2: Create a new session for Non-Interactive User
    @POST("api/v1/auth/session")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json", // Ensured Content-Type for JSON body
        // As per your original code, keep these if they are required by myPOS
        "X-Application-Id: mps-app-30000837",
        "X-Partner-Id: mps-p-10000107"
    )
    suspend fun getAuthSession( // Changed to suspend
        @Header("Authorization") authorization: String, // For the Bearer token from Step 1
        @Body body: AuthSessionRequest // JSON body with client_id and client_secret
    ): Response<AuthSessionData> // Changed to Response<T>

    // Note: The original getOAuthToken with username/password has been removed
    // as the API docs specify client_credentials grant type for /oauth/token.
    // If you have a different endpoint for username/password login, you'd add it separately.
}