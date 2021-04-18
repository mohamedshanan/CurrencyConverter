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

package com.shannan.converter.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shannan.converter.data.Rate
import com.shannan.converter.data.RatesResponse

/**
 * Data Access Object for the rates table.
 */
@Dao
interface RatesDao {

    /**
     * Observes list of rates.
     *
     * @return all rates.
     */
    @Query("SELECT * FROM rates")
    fun observeRates(): LiveData<List<Rate>>

    /**
     * Observes a single rate.
     *
     * @param currency the three chars currency.
     * @return the rate with currency.
     */
    @Query("SELECT * FROM rates WHERE currency = :currency")
    fun observeRateByCurrency(currency: String): LiveData<Rate>

    /**
     * Select all rates from the rates table.
     *
     * @return all rates.
     */
    @Query("SELECT * FROM rates")
    suspend fun getRates(): List<Rate>

    /**
     * Select a rate by currency.
     *
     * @param currency the currency.
     * @return the rate with currency.
     */
    @Query("SELECT * FROM rates WHERE currency = :currency")
    suspend fun getRateByCurrency(currency: String): Rate?

    /**
     * Insert a rate in the database. If the rate already exists, replace it.
     *
     * @param rate the rate to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: Rate)

    /**
     * Update a rate.
     *
     * @param rate to be updated
     * @return the number of rates updated. This should always be 1.
     */
    @Update
    suspend fun updateRate(rate: Rate): Int

    /**
     * Delete a rate by currency.
     *
     * @return the number of rates deleted. This should always be 1.
     */
    @Query("DELETE FROM rates WHERE currency = :currency")
    suspend fun deleteRateByCurrency(currency: String): Int

    /**
     * Delete all rates.
     */
    @Query("DELETE FROM rates")
    suspend fun deleteRates()
}
