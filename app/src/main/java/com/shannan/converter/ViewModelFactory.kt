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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.domain.GetRatesUseCase
import com.shannan.converter.domain.ObserveRateUseCase
import com.shannan.converter.domain.ObserveRatesUseCase
import com.shannan.converter.domain.RefreshRatesUseCase
import com.shannan.converter.rateconverter.RateConverterViewModel
import com.shannan.converter.rates.RatesViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val ratesRepository: RatesRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {

                    isAssignableFrom(RateConverterViewModel::class.java) ->
                        RateConverterViewModel(
                            ObserveRateUseCase(ratesRepository)
                        )

                    isAssignableFrom(RatesViewModel::class.java) ->
                        RatesViewModel(
                            ObserveRatesUseCase(ratesRepository),
                            GetRatesUseCase(ratesRepository)
                        )
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}
