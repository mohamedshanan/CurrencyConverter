/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shannan.converter.data.source.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.shannan.converter.BuildConfig
import com.shannan.converter.data.Rate
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.data.source.RatesDataSource
import retrofit2.Retrofit
import java.util.*
import com.shannan.converter.data.Result as ConverterResult

/**
 * Implementation of the data source that adds a latency simulating network.
 */
class RatesRemoteDataSource constructor(retrofit: Retrofit) : RatesDataSource {

    private lateinit var RATES_SERVICE_DATA : RatesResponse

    private val ratesApi by lazy { retrofit.create(RatesApi::class.java) }

    private val observableRates = MutableLiveData<ConverterResult<List<Rate>>>()

    override suspend fun refreshRates() {
        val result = getRates()
        observableRates.value = when (result) {
            is ConverterResult.Success -> ConverterResult.Success(result.data.rates)
            is ConverterResult.Error -> ConverterResult.Error(Exception("Error Getting Rates"))
            ConverterResult.Loading -> ConverterResult.Loading
        }
    }

    override fun observeRate(currency: String): LiveData<ConverterResult<Rate>> {
        return observableRates.map { rates ->
            when (rates) {
                is ConverterResult.Loading -> ConverterResult.Loading
                is ConverterResult.Error -> ConverterResult.Error(rates.exception)
                is ConverterResult.Success -> {
                    val rate = rates.data.firstOrNull() { it.currency == currency }
                            ?: return@map ConverterResult.Error(Exception("Not found"))
                    ConverterResult.Success(rate)
                }
            }
        }
    }

    override fun observeRates(): LiveData<ConverterResult<List<Rate>>> {
        return observableRates
    }

    override suspend fun getRates(): ConverterResult<RatesResponse> {
        return ConverterResult.Success(ratesApi.getLatestRates(BuildConfig.FIXER_API_KEY))
    }

    override suspend fun getRate(currency: String): ConverterResult<Rate> {
        // Simulate network by delaying the execution.
        RATES_SERVICE_DATA.rates.find {
            it.currency == currency
        }?.let { return ConverterResult.Success(it) }
        return ConverterResult.Error(Exception("Rate not found"))
    }

    override suspend fun saveRate(rate: Rate) {
        TODO("Not yet implemented")
    }

    override suspend fun saveBaseCurrency(base: String) {
        RATES_SERVICE_DATA.base = base
    }

    override suspend fun saveTimeStamp(timeStamp: Long) {
        RATES_SERVICE_DATA.timestamp = timeStamp
    }

    override fun getTimeStamp() = RATES_SERVICE_DATA.timestamp

    override fun getBaseCurrency()= RATES_SERVICE_DATA.base

    override suspend fun deleteAllRates() {
        RATES_SERVICE_DATA.rates = emptyList()
    }


}
