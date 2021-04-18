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
package com.shannan.converter.data

/**
 *
 * @param timestamp a standard UNIX time stamp indicating the time the given exchange rate data was collected
 * @param base the three-letter currency code of the base currency
 * @param date the date the given exchange rate data was collected
 * @param rates the actual exchange rate data
 */
data class RatesResponse(var timestamp: Long = 0L,
        var base: String = "",
        var date: String = "",
        var rates: List<Rate>
)
