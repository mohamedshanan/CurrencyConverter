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

package com.shannan.converter.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.source.RatesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ObserveRatesUseCase(
        private val ratesRepository: RatesRepository,
        private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke(): LiveData<Result<List<Rate>>> = withContext(dispatcher) {
        ratesRepository.observeRates().map {
            if (it is Success) {
                Success(it.data)
            } else {
                it
            }
        }
    }
}
