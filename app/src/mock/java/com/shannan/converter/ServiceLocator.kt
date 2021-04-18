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
package com.shannan.converter
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.shannan.converter.data.FakeRatesRemoteDataSource
import com.shannan.converter.data.source.DefaultRatesRepository
import com.shannan.converter.data.source.RatesDataSource
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.data.source.local.RatesLocalDataSource
import com.shannan.converter.data.source.local.RatesDatabase
import kotlinx.coroutines.runBlocking

/**
 * A Service Locator for the [RatesRepository]. This is the mock version, with a
 * [FakeRatesRemoteDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: RatesDatabase? = null
    @Volatile
    var ratesRepository: RatesRepository? = null
        @VisibleForTesting set

    fun provideRatesRepository(context: Context): RatesRepository {
        synchronized(this) {
            return ratesRepository ?: ratesRepository ?: createRatesRepository(context)
        }
    }

    private fun createRatesRepository(context: Context): RatesRepository {
        val newRepo = DefaultRatesRepository(FakeRatesRemoteDataSource, createRatesLocalDataSource(context))
        ratesRepository = newRepo
        return newRepo
    }

    private fun createRatesLocalDataSource(context: Context): RatesDataSource {
        val database = database ?: createDataBase(context)
        return RatesLocalDataSource(database.ratesDao())
    }

    private fun createDataBase(context: Context): RatesDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RatesDatabase::class.java, "Rates.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                FakeRatesRemoteDataSource.deleteAllRates()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            ratesRepository = null
        }
    }
}
