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
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.data.Result

/**
 * Interface to the data layer.
 */
interface RatesRepository {

    fun observeRates(): LiveData<Result<List<Rate>>>

    suspend fun getRates(): Result<List<Rate>>

    suspend fun refreshRates()

    fun observeRate(currency: String): LiveData<Result<Rate>>

    suspend fun getRate(currency: String): Result<Rate>

    suspend fun deleteAllRates()

    fun getBaseCurrency(): String

}
