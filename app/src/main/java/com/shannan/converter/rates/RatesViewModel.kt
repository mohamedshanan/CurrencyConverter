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
package com.shannan.converter.rates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shannan.converter.Event
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.domain.ObserveRatesUseCase
import com.shannan.converter.R
import com.shannan.converter.domain.GetRatesUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the task list screen.
 */
class RatesViewModel(
        private val observeRatesUseCase: ObserveRatesUseCase,
        private val getRatesUseCase: GetRatesUseCase
) : ViewModel() {

    private val _items: LiveData<Result<List<Rate>>> =
            liveData {
                emit(Result.Loading)
                emitSource(observeRatesUseCase())
            }


    // Exposed items
    val items: LiveData<List<Rate>> = _items.map {
        when (it) {
            is Success -> {
                dataLoading.value = false
                setBaseCurrency(it.data)
            }
            is Result.Error -> {
                dataLoading.value = false
                showSnackbarMessage(R.string.loading_rates_error)
                emptyList()
            }
            is Result.Loading -> {
                dataLoading.value = true
                emptyList()
            }
        }
    }

    val dataLoading = MutableLiveData(false)

    private val _noRatesIconRes = MutableLiveData<Int>()
    val noRatesIconRes: LiveData<Int> = _noRatesIconRes

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _openConverterEvent = MutableLiveData<Event<String>>()
    val openConverterEvent: LiveData<Event<String>> = _openConverterEvent

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = _items.map {
        (it as? Success)?.data?.isEmpty() ?: true
    }

    init {
        // Set initial state
        _noRatesIconRes.value = R.mipmap.ic_no_money
        loadRates()
    }

    /**
     * Called by Data Binding.
     */
    fun openRateConverter(currency: String) {
        _openConverterEvent.value = Event(currency)
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }


    fun loadRates() {
        dataLoading.value = true
        viewModelScope.launch {
            getRatesUseCase()
        }
    }

    private fun setBaseCurrency(list: List<Rate>): List<Rate> {
        val baseIndex = list.indexOfFirst { it.currency == "EUR" }
        if (baseIndex > -1){
            val base = list[baseIndex]
            val newList = arrayListOf<Rate>()
            newList.add(base)
            newList.addAll(list)
            return newList
        }
        return list
    }
}
