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
package com.shannan.converter.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.shannan.converter.data.Result.Error
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.source.RatesDataSource

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
object FakeRatesRemoteDataSource : RatesDataSource {

    private lateinit var ratesServiceData: RatesResponse

    private val observableRates = MutableLiveData<Result<List<Rate>>>()

    override fun observeRates(): LiveData<Result<List<Rate>>> {
        return observableRates
    }

    override fun observeRate(currency: String): LiveData<Result<Rate>> {
        return observableRates.map { rates ->
            when (rates) {
                is Result.Loading -> Result.Loading
                is Error -> Error(rates.exception)
                is Success -> {
                    val rate = rates.data.firstOrNull() { it.currency == currency }
                            ?: return@map Error(Exception("Not found"))
                    Success(rate)
                }
            }
        }
    }

    override suspend fun getRate(currency: String): Result<Rate> {

        val rate = ratesServiceData.rates.find { it.currency == currency }
        rate?.let { return Success(it) }
        return Error(Exception("Could not find rate"))
    }

    override suspend fun getRates(): Result<RatesResponse> {
        val timestamp = 0L
        return Success(RatesResponse(timestamp = timestamp, rates = ratesServiceData.rates))
    }

    override suspend fun refreshRates() {
        getRates()
    }

    override suspend fun saveRate(rate: Rate) {
        ratesServiceData.rates.find { it.currency == rate.currency }?.value = rate.value
    }

    override suspend fun saveTimeStamp(timestamp: Long) {
        ratesServiceData.timestamp = timestamp
    }


    override suspend fun deleteAllRates() {
        ratesServiceData = RatesResponse(rates = emptyList())
        refreshRates()
    }
}
