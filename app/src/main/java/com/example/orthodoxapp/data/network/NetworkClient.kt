package com.example.orthodoxapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.orthodoxapp.security.SecurityManager

/**
 * Network Client - The Central Connection Hub
 */
object NetworkClient {
    
    private const val BASE_URL = "http://10.0.2.2:5000/api/"

    // AUTH INTERCEPTOR: Automatically attaches JWT to all requests
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = SecurityManager.getAuthToken()

        val requestBuilder = original.newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
