package com.example.smartsave.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.smartsave.data.api.MyPosApiService

object RetrofitClient {
    private const val BASE_URL = "https://demo-api-gateway.mypos.com/" // <<< IS THIS CORRECT?

    val apiService: MyPosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyPosApiService::class.java)
    }
}
