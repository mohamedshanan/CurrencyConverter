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

package com.shannan.converter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.data.source.RatesDataSource

object FakeFailingRatesRemoteDataSource : RatesDataSource {
    override suspend fun getRates(): Result<RatesResponse> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun getRate(rateId: String): Result<Rate> {
        return Result.Error(Exception("Test"))
    }

    override fun observeRates(): LiveData<Result<List<Rate>>> {
        val observableRates = MutableLiveData<Result<List<Rate>>>()
        observableRates.value = Result.Error(Exception("Rates not found"))
        return observableRates
    }

    override suspend fun refreshRates() {
        TODO("not implemented")
    }

    override fun observeRate(rateId: String): LiveData<Result<Rate>> {
        return liveData { emit(getRate(rateId)) }
    }


    override suspend fun deleteAllRates() {
        TODO("not implemented")
    }

    override suspend fun saveRate(rate: Rate) {
        TODO("Not yet implemented")
    }

    override suspend fun saveBaseCurrency(base: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveTimeStamp(timestamp: Long) {
        TODO("Not yet implemented")
    }

    override fun getTimeStamp(): Long {
        TODO("Not yet implemented")
    }

    override fun getBaseCurrency(): String {
        TODO("Not yet implemented")
    }

}
