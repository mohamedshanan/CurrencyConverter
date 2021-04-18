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

package com.shannan.converter.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Error
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.RatesResponse

class FakeDataSource(var ratesResponse: RatesResponse) : RatesDataSource {

    private val observableRates = MutableLiveData<Result<List<Rate>>>()

    override suspend fun getRates(): Result<RatesResponse> {
        ratesResponse?.let { return Success(it) }
        return Error(
                Exception("Rates not found")
        )
    }

    override suspend fun getRate(currency: String): Result<Rate> {
        ratesResponse.rates.find { it.currency == currency }?.let { return Success(it) }
        return Error(
                Exception("Rate not found")
        )
    }

    override suspend fun saveRate(rate: Rate) {
        ratesResponse?.rates.toMutableList().add(rate)
    }

    override suspend fun saveBaseCurrency(base: String) {
        ratesResponse.base = base
    }

    override suspend fun saveTimeStamp(timestamp: Long) {
        ratesResponse.timestamp = timestamp
    }

    override fun getTimeStamp() = ratesResponse.timestamp

    override fun getBaseCurrency() = ratesResponse.base

    override suspend fun deleteAllRates() {
        ratesResponse = RatesResponse(rates = emptyList())
    }

    override fun observeRates(): LiveData<Result<List<Rate>>> {
        return observableRates
    }

    override suspend fun refreshRates() {
        observableRates.value = Success(ratesResponse.rates)
    }

    override fun observeRate(currency: String): LiveData<Result<Rate>> {

        val rateObservable = MutableLiveData<Result<Rate>>()
        val foundRate = ratesResponse.rates.find { it.currency == currency }
        if (foundRate != null) {
            rateObservable.value = Success(foundRate)
        } else {
            rateObservable.value = Error(Exception("Rate not found"))
        }
        return rateObservable
    }

}
