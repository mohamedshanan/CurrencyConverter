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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Error
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.RatesResponse
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : RatesRepository {

    var tasksServiceData: LinkedHashMap<String, Rate> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableTasks = MutableLiveData<Result<List<Rate>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun refreshRates() {
        observableTasks.postValue(getRates())
    }

    override fun observeRates(): LiveData<Result<List<Rate>>> {
        runBlocking { refreshRates() }
        return observableTasks
    }

    override fun observeRate(currency: String): LiveData<Result<Rate>> {
        runBlocking { refreshRates() }
        return observableTasks.map { tasks ->
            when (tasks) {
                is Result.Loading -> Result.Loading
                is Error -> Error(tasks.exception)
                is Success -> {
                    val task = tasks.data.firstOrNull { it.currency == currency }
                        ?: return@map Error(Exception("Not found"))
                    Success(task)
                }
            }
        }
    }

    override suspend fun getRate(currency: String): Result<Rate> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        tasksServiceData[currency]?.let {
            return Success(it)
        }
        return Error(Exception("Could not find task"))
    }

    override suspend fun getRates(forceUpdate: Boolean): Result<List<Rate>> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        return Success(tasksServiceData.values.toList())
    }

    override suspend fun deleteAllRates() {
        tasksServiceData.clear()
        refreshRates()
    }

    @VisibleForTesting
    fun addRates(vararg rates: Rate) {
        for (rate in rates) {
            tasksServiceData[rate.currency] = rate
        }
        runBlocking { refreshRates() }
    }
}
