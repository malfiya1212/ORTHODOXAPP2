package com.example.orthodoxapp.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApiService {
    @GET("latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String): Response<ExchangeRateResponse>
}

data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val time_last_updated: Long,
    val rates: Map<String, Double>
)
