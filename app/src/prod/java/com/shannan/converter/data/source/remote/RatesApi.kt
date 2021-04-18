package com.shannan.converter.data.source.remote

import com.shannan.converter.data.RatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface RatesApi {

    @GET("latest")
    suspend fun getLatestRates(@Query("access_key") apiKey: String,
                               @Query("base") base: String = "EUR"): RatesResponse
}
