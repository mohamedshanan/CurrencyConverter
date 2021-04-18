package com.shannan.converter.domain

import com.shannan.converter.data.Rate
import com.shannan.converter.data.Result
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.util.wrapEspressoIdlingResource

class GetRateUseCase(
    private val ratesRepository: RatesRepository
) {
    suspend operator fun invoke(currency: String): Result<Rate> {

        wrapEspressoIdlingResource {
            return ratesRepository.getRate(currency)
        }
    }
}