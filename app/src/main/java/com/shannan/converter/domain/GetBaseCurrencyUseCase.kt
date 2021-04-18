package com.shannan.converter.domain

import com.shannan.converter.data.Result
import com.shannan.converter.data.Result.Success
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.util.wrapEspressoIdlingResource

/**
 * Not used. Left for sample completeness.
 */
class GetBaseCurrencyUseCase(
    private val ratesRepository: RatesRepository
) {
    operator fun invoke(): Result<String> {

        wrapEspressoIdlingResource {
            return Success(ratesRepository.getBaseCurrency())
        }
    }
}
