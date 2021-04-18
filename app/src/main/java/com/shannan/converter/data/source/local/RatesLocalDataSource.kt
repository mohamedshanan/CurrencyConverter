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
package com.shannan.converter.data.source.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Error
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.data.source.RatesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of a data source as a db.
 */
class RatesLocalDataSource internal constructor(
        private val ratesDao: RatesDao,
        private val sharedPreferences: SharedPreferences,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RatesDataSource {



    override fun observeRates(): LiveData<Result<List<Rate>>> {
        return ratesDao.observeRates().map {
            Success(it)
        }
    }


    override suspend fun getRates(): Result<RatesResponse> = withContext(ioDispatcher) {
        return@withContext try {
            val timestamp = 0L
            Success(RatesResponse(timestamp = timestamp, rates = ratesDao.getRates()))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun refreshRates() {
        getRates()
    }

    override fun observeRate(currency: String): LiveData<Result<Rate>> {
        return ratesDao.observeRateByCurrency(currency).map {
            Success(it)
        }
    }

    override suspend fun getRate(currency: String): Result<Rate> = withContext(ioDispatcher) {
        try {
            val task = ratesDao.getRateByCurrency(currency)
            if (task != null) {
                return@withContext Success(task)
            } else {
                return@withContext Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveRate(rate: Rate) = withContext(ioDispatcher) {
        ratesDao.insertRate(rate)
    }

    override suspend fun saveBaseCurrency(base: String) {
        with (sharedPreferences.edit()) {
            putString(BASE_CURRENCY_KEY, base)
            apply()
        }
    }

    override suspend fun deleteAllRates() = withContext(ioDispatcher) {
        ratesDao.deleteRates()
    }

    override suspend fun saveTimeStamp(timestamp: Long){
        with (sharedPreferences.edit()) {
            putLong(TIMESTAMP_KEY, timestamp)
            apply()
        }
    }

    override fun getTimeStamp(): Long {
        return sharedPreferences.getLong(TIMESTAMP_KEY, 0L)
    }

    override fun getBaseCurrency(): String {
        return sharedPreferences.getString(BASE_CURRENCY_KEY, "EUR")
    }

}
const val TIMESTAMP_KEY = "RATES_TIME_STAMP"
const val BASE_CURRENCY_KEY = "BASE_CURRENCY"