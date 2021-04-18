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
package com.shannan.converter.rateconverter

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.shannan.converter.Event
import com.shannan.converter.R
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.domain.ObserveRateUseCase
import kotlin.time.times

/**
 * ViewModel for the Converter screen.
 */
class RateConverterViewModel(
        private val observeRateUseCase: ObserveRateUseCase
) : ViewModel() {

    private val _currency = MutableLiveData<String>()

    private val _rate = _currency.switchMap { currency ->
        observeRateUseCase(currency).map { computeResult(it) }
    }
    val rate: LiveData<Rate?> = _rate

    private val _conversionResult = MutableLiveData<Double>()
    val conversionResult: LiveData<Double> = _conversionResult

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText


    fun start(currency: String?) {
        // If we're already loading or already loaded, return (might be a config change)
        if (_dataLoading.value == true || currency == _currency.value) {
            return
        }
        // Trigger the load
        _currency.value = currency
    }

    private fun computeResult(ratesResult: Result<Rate>): Rate? {
        return if (ratesResult is Success) {
            _conversionResult.value = ratesResult.data.value
            ratesResult.data
        } else {
            showSnackbarMessage(R.string.loading_rates_error)
            null
        }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s != null && s.isNotEmpty()){
            _conversionResult.value = rate.value?.value?.let { s.toString().toDouble().times(it) }
        }
    }
}
