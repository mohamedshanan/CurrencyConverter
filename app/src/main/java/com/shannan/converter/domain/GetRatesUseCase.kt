package com.shannan.converter.domain

import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.util.wrapEspressoIdlingResource

/**
 * Not used. Left for sample completeness.
 */
class GetRatesUseCase(
    private val ratesRepository: RatesRepository
) {
    suspend operator fun invoke(): Result<List<Rate>> {

        wrapEspressoIdlingResource {

            val ratesResult = ratesRepository.getRates()
            if (ratesResult is Success) {
                return Success(ratesResult.data)
            }
            return ratesResult
        }
    }
}
