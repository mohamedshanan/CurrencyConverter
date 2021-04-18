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

import com.shannan.converter.MainCoroutineRule
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.google.common.truth.Truth.assertThat
import com.shannan.converter.data.Rate
import com.shannan.converter.data.RatesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultRatesRepositoryTest {

    private val rate1 = Rate("USD", 1.198235)
    private val rate2 = Rate("EGP", 18.786627)
    private val rate3 = Rate("AED", 4.401362)
    private val remoteRates = listOf(rate1, rate2, rate3)
    private val localRates = listOf(rate2, rate3)
    private val newRates = listOf(rate1)
    private val remoteRatesResponse = RatesResponse(rates = remoteRates)
    private val localRatesResponse = RatesResponse(rates = localRates)
    private lateinit var ratesRemoteDataSource: FakeDataSource
    private lateinit var ratesLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var ratesRepository: DefaultRatesRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        ratesRemoteDataSource = FakeDataSource(remoteRatesResponse)
        ratesLocalDataSource = FakeDataSource(localRatesResponse)
        // Get a reference to the class under test
        ratesRepository = DefaultRatesRepository(
            ratesRemoteDataSource, ratesLocalDataSource, Dispatchers.Main
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getrates_emptyRepositoryAndUninitializedCache() = mainCoroutineRule.runBlockingTest {
        val emptySource = FakeDataSource(RatesResponse(rates = emptyList()))
        val ratesRepository = DefaultRatesRepository(
            emptySource, emptySource, Dispatchers.Main
        )

        assertThat(ratesRepository.getRates() is Success).isTrue()
    }

    @Test
    fun getrates_repositoryCachesAfterFirstApiCall() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = ratesRepository.getRates()

        ratesRemoteDataSource.ratesResponse = RatesResponse(rates = newRates)

        val second = ratesRepository.getRates()

        // Initial and second should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getrates_requestsAllratesFromRemoteDataSource() = mainCoroutineRule.runBlockingTest {
        // When rates are requested from the rates repository
        val rates = ratesRepository.getRates(true) as Success

        // Then rates are loaded from the remote data source
        assertThat(rates.data).isEqualTo(remoteRates)
    }

    @Test
    fun getrates_WithDirtyCache_ratesAreRetrievedFromRemote() = mainCoroutineRule.runBlockingTest {
        // First call returns from REMOTE
        val rates = ratesRepository.getRates()

        // Set a different list of rates in REMOTE
        ratesRemoteDataSource.ratesResponse.rates = newRates.toMutableList()

        // But if rates are cached, subsequent calls load from cache
        val cachedrates = ratesRepository.getRates()
        assertThat(cachedrates).isEqualTo(rates)

        // Now force remote loading
        val refreshedrates = ratesRepository.getRates(true) as Success

        // rates must be the recently updated in REMOTE
        assertThat(refreshedrates.data).isEqualTo(newRates)
    }

    @Test
    fun getrates_WithDirtyCache_remoteUnavailable_error() = mainCoroutineRule.runBlockingTest {
        // Make remote data source unavailable
        ratesRemoteDataSource.ratesResponse = RatesResponse(rates = emptyList())

        // Load rates forcing remote load
        val refreshedrates = ratesRepository.getRates(true)

        // Result should be an error
        assertThat(refreshedrates).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getrates_WithRemoteDataSourceUnavailable_ratesAreRetrievedFromLocal() =
        mainCoroutineRule.runBlockingTest {
            // When the remote data source is unavailable
            ratesRemoteDataSource.ratesResponse = RatesResponse(rates = emptyList())

            // The repository fetches from the local source
            assertThat((ratesRepository.getRates() as Success).data).isEqualTo(localRates)
        }


    @Test
    fun getrates_refreshesLocalDataSource() = mainCoroutineRule.runBlockingTest {
        val initialLocal = ratesLocalDataSource.ratesResponse

        // First load will fetch from remote
        val newrates = (ratesRepository.getRates(true) as Success).data

        assertThat(newrates).isEqualTo(remoteRates)
        assertThat(newrates).isEqualTo(ratesLocalDataSource.ratesResponse)
        assertThat(ratesLocalDataSource.ratesResponse).isEqualTo(initialLocal)
    }


    @Test
    fun getrate_repositoryCachesAfterFirstApiCall() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote
        ratesRemoteDataSource.ratesResponse.rates = mutableListOf(rate1)
        ratesRepository.getRate(rate1.currency)

        // Configure the remote data source to store a different rate
        ratesRemoteDataSource.ratesResponse.rates = mutableListOf(rate2)

        val rate1SecondTime = ratesRepository.getRate(rate1.currency) as Success
        val rate2SecondTime = ratesRepository.getRate(rate2.currency) as Success

        // Both work because one is in remote and the other in cache
        assertThat(rate1SecondTime.data.currency).isEqualTo(rate1.currency)
        assertThat(rate2SecondTime.data.currency).isEqualTo(rate2.currency)
    }

    @Test
    fun getrate_forceRefresh() = mainCoroutineRule.runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        ratesRemoteDataSource.ratesResponse.rates = mutableListOf(rate1)
        ratesRepository.getRate(rate1.currency)

        // Configure the remote data source to return a different rate
        ratesRemoteDataSource.ratesResponse.rates = mutableListOf(rate2)

        // Force refresh
        val rate1SecondTime = ratesRepository.getRate(rate1.currency)
        val rate2SecondTime = ratesRepository.getRate(rate2.currency)

        // Only rate2 works because the cache and local were invalidated
        assertThat((rate1SecondTime as? Success)?.data?.currency).isNull()
        assertThat((rate2SecondTime as? Success)?.data?.currency).isEqualTo(rate2.currency)
    }

    @Test
    fun deleteAllrates() = mainCoroutineRule.runBlockingTest {
        val initialrates = (ratesRepository.getRates() as? Success)?.data

        // Delete all rates
        ratesRepository.deleteAllRates()

        // Fetch data again
        val afterDeleterates = (ratesRepository.getRates() as? Success)?.data

        // Verify rates are empty now
        assertThat(initialrates).isNotEmpty()
        assertThat(afterDeleterates).isEmpty()
    }
}
