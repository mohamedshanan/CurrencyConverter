package com.shannan.converter.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result.Error
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.source.FakeRepository
import com.shannan.converter.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [GetRatesUseCase].
 */
@ExperimentalCoroutinesApi
class ObserveRatesUseCaseTest {

    private val ratesRepository = FakeRepository()

    // Executes each rate synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Not needed here but it's preferred to have control of dispatchers from test.
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    // Class under test.
    private val observeRatesUseCase = ObserveRatesUseCase(ratesRepository, testCoroutineDispatcher)

    @Test
    fun loadRates_empty() = runBlockingTest {
        // Given an empty repository

        // When calling the use case
        val result = observeRatesUseCase().getOrAwaitValue()

        // Verify the result is a success and empty
        assertTrue(result is Success)
        assertTrue((result as Success).data.isEmpty())
    }

    @Test
    fun loadRates_error() = runBlockingTest {
        // Make the repository return errors
        ratesRepository.setReturnError(true)

        // Load rates
        val result = observeRatesUseCase().getOrAwaitValue()

        // Verify the result is an error
        assertTrue(result is Error)
    }

    @Test
    fun loadRates() = runBlockingTest {
        // Given a repository with 1 active and 2 completed rates:
        ratesRepository.addRates(
            Rate("USD", 0.95), Rate("EGP", 17.75)
        )

        // Load rates
        val result = observeRatesUseCase().getOrAwaitValue()

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 3)
    }
}
