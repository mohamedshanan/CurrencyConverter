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
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.util.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [RatesRepository]. Single entry point for managing rates' data.
 */
class DefaultRatesRepository(
        private val ratesRemoteDataSource: RatesDataSource,
        private val ratesLocalDataSource: RatesDataSource,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RatesRepository {

    override suspend fun getRates(): Result<List<Rate>> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {
            // If the local rates are older than one hour we should load remote rates
            if (System.currentTimeMillis().minus(ratesLocalDataSource.getTimeStamp() * 1000) > 3600000) {
                try {
                    updateRatesFromRemoteDataSource()
                } catch (ex: Exception) {
                    return Result.Error(ex)
                }
            }
            val response = ratesLocalDataSource.getRates()
            return if (response is Success) {
                Success(response.data.rates)
            } else {
                Result.Error(Exception("Error loading rates"))
            }
        }
    }

    override suspend fun refreshRates() {
        updateRatesFromRemoteDataSource()
    }

    override fun observeRates(): LiveData<Result<List<Rate>>> {
        return ratesLocalDataSource.observeRates()
    }

    private suspend fun updateRatesFromRemoteDataSource() {
        val remoteRates = ratesRemoteDataSource.getRates()

        if (remoteRates is Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each task.
            ratesLocalDataSource.deleteAllRates()
            remoteRates.data.rates.forEach {
                ratesLocalDataSource.saveRate(it)
            }
            ratesLocalDataSource.saveTimeStamp(remoteRates.data.timestamp)

        } else if (remoteRates is Result.Error) {
            throw remoteRates.exception
        }
    }

    override fun observeRate(currecy: String): LiveData<Result<Rate>> {
        return ratesLocalDataSource.observeRate(currecy)
    }

    /**
     * Relies on [getRate] to fetch data and picks the rate with the same currency.
     */
    override suspend fun getRate(currency: String): Result<Rate> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {
            return ratesLocalDataSource.getRate(currency)
        }
    }

    override suspend fun deleteAllRates() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { ratesLocalDataSource.deleteAllRates() }
            }
        }
    }

    override fun getBaseCurrency(): String {
        return ratesLocalDataSource.getBaseCurrency()
    }
}
