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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.shannan.converter.MainCoroutineRule
import com.shannan.converter.data.source.FakeRepository
import com.shannan.converter.domain.ObserveRateUseCase
import com.shannan.converter.getOrAwaitValue
import com.shannan.converter.observeForTesting
import com.google.common.truth.Truth.assertThat
import com.shannan.converter.data.Rate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [RateConverterViewModel]
 */
@ExperimentalCoroutinesApi
class RatesResponseDetailViewModelTest {

    // Subject under test
    private lateinit var rateConverterViewModel: RateConverterViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var ratesRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each rate synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val rate = Rate("EGP", 18.786627)

    @Before
    fun setupViewModel() {
        ratesRepository = FakeRepository()
        ratesRepository.addRates(rate)

        rateConverterViewModel = RateConverterViewModel(
            ObserveRateUseCase(ratesRepository)
        )
    }

    @Test
    fun getActiveRateFromRepositoryAndLoadIntoView() {
        rateConverterViewModel.start(rate.currency)

        // Then verify that the view was notified
        assertThat(rateConverterViewModel.rate.getOrAwaitValue()?.currency).isEqualTo(rate.currency)
        assertThat(rateConverterViewModel.rate.getOrAwaitValue()?.value)
            .isEqualTo(rate.value)
    }

    @Test
    fun rateConverterViewModel_repositoryError() {
        // Given a repository that returns errors
        ratesRepository.setReturnError(true)

        // Given an initialized ViewModel with an active rate
        rateConverterViewModel.start(rate.currency)
        // Get the computed LiveData value
        rateConverterViewModel.rate.observeForTesting {
            // Then verify that data is not available
            assertThat(rateConverterViewModel.rate.getOrAwaitValue()).isNotNull()
        }
    }

    @Test
    fun updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        val snackbarText = rateConverterViewModel.snackbarText.value

        // Check that the value is null
        assertThat(snackbarText).isNull()
    }


    @Test
    fun loadRate_loading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Load the rate in the viewmodel
        rateConverterViewModel.start(rate.currency)
        // Start observing to compute transformations
        rateConverterViewModel.rate.observeForTesting {


            // Then progress indicator is shown
            assertThat(rateConverterViewModel.dataLoading.getOrAwaitValue()).isTrue()

            // Execute pending coroutines actions
            mainCoroutineRule.resumeDispatcher()

            // Then progress indicator is hidden
            assertThat(rateConverterViewModel.dataLoading.getOrAwaitValue()).isFalse()
        }
    }
}
