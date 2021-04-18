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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.shannan.converter.MainCoroutineRule
import com.shannan.converter.assertLiveDataEventTriggered
import com.shannan.converter.assertSnackbarMessage
import com.shannan.converter.data.source.FakeRepository
import com.shannan.converter.domain.ObserveRatesUseCase
import com.shannan.converter.domain.RefreshRatesUseCase
import com.shannan.converter.getOrAwaitValue
import com.shannan.converter.observeForTesting
import com.google.common.truth.Truth.assertThat
import com.shannan.converter.data.Rate
import com.shannan.converter.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [RatesViewModel]
 */
@ExperimentalCoroutinesApi
class RatesViewModelTest {

    // Subject under test
    private lateinit var ratesViewModel: RatesViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var ratesRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each rate synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setupViewModel() {
        // We initialise the rates to 3
        ratesRepository = FakeRepository()
        val rate1 = Rate("USD", 1.198235)
        val rate2 = Rate("EGP", 18.786627)
        val rate3 = Rate("AED", 4.401362)
        ratesRepository.addRates(rate1, rate2, rate3)

        ratesViewModel = RatesViewModel(
            ObserveRatesUseCase(ratesRepository, testCoroutineDispatcher),
            RefreshRatesUseCase(ratesRepository)
        )
    }

    @Test
    fun loadAllRatesFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        testCoroutineDispatcher.pauseDispatcher()

        // Trigger loading of rates
        ratesViewModel.loadRates()
        // Observe the items to keep LiveData emitting
        ratesViewModel.items.observeForTesting {

            // Then progress indicator is shown
            assertThat(ratesViewModel.dataLoading.getOrAwaitValue(4)).isTrue()

            // Execute pending coroutines actions
            testCoroutineDispatcher.resumeDispatcher()

            // Then progress indicator is hidden
            assertThat(ratesViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And data correctly loaded
            assertThat(ratesViewModel.items.getOrAwaitValue()).hasSize(3)
        }
    }

    @Test
    fun loadActiveRatesFromRepositoryAndLoadIntoView() {
        // Load rates
        ratesViewModel.loadRates()
        // Observe the items to keep LiveData emitting
        ratesViewModel.items.observeForTesting {

            // Then progress indicator is hidden
            assertThat(ratesViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And data correctly loaded
            assertThat(ratesViewModel.items.getOrAwaitValue()).hasSize(1)
        }
    }

    @Test
    fun loadCompletedRatesFromRepositoryAndLoadIntoView() {
        // Load rates
        ratesViewModel.loadRates()
        // Observe the items to keep LiveData emitting
        ratesViewModel.items.observeForTesting {

            // Then progress indicator is hidden
            assertThat(ratesViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And data correctly loaded
            assertThat(ratesViewModel.items.getOrAwaitValue()).hasSize(2)
        }
    }

    @Test
    fun loadRates_error() {
        // Make the repository return errors
        ratesRepository.setReturnError(true)

        // Load rates
        ratesViewModel.loadRates()
        // Observe the items to keep LiveData emitting
        ratesViewModel.items.observeForTesting {

            // Then progress indicator is hidden
            assertThat(ratesViewModel.dataLoading.getOrAwaitValue()).isFalse()

            // And the list of items is empty
            assertThat(ratesViewModel.items.getOrAwaitValue()).isEmpty()

            // And the snackbar updated
            assertSnackbarMessage(ratesViewModel.snackbarText, R.string.loading_rates_error)
        }
    }

    @Test
    fun clickOnOpenRate_setsEvent() {
        // When opening a new rate
        val currency = "USD"
        ratesViewModel.openRateConverter(currency)

        // Then the event is triggered
        assertLiveDataEventTriggered(ratesViewModel.openConverterEvent, currency)
    }

}
