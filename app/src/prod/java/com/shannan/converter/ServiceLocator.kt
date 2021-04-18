/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.shannan.converter.data.RatesResponse
import com.shannan.converter.data.source.DefaultRatesRepository
import com.shannan.converter.data.source.RatesDataSource
import com.shannan.converter.data.source.RatesRepository
import com.shannan.converter.data.source.local.RatesDatabase
import com.shannan.converter.data.source.local.RatesLocalDataSource
import com.shannan.converter.data.source.remote.RatesDeserializer
import com.shannan.converter.data.source.remote.RatesRemoteDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A Service Locator for the [RatesRepository]. This is the prod version, with a
 * the "real" [RatesDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: RatesDatabase? = null
    private var sharedPreferences: SharedPreferences? = null

    // Custom Rates Converter for Retrofit
    private val retrofit = Retrofit.Builder()
            .baseUrl("http://data.fixer.io/api/")
            .client(createClient())
            .addConverterFactory(buildGsonConverterFactory())
            .build()

    private fun buildGsonConverterFactory(): GsonConverterFactory {
        val gsonBuilder = GsonBuilder()
        // Custom Rates Converter for Retrofit
        gsonBuilder.registerTypeAdapter(RatesResponse::class.java, RatesDeserializer())
        return GsonConverterFactory.create(gsonBuilder.create())
    }

    @Volatile
    var ratesRepository: RatesRepository? = null
        @VisibleForTesting set

    fun provideRatesRepository(context: Context): RatesRepository {
        synchronized(this) {
            return ratesRepository ?: ratesRepository ?: createRatesRepository(context)
        }
    }

    private fun createRatesRepository(context: Context): RatesRepository {
        val newRepo = DefaultRatesRepository(RatesRemoteDataSource(retrofit), createRatesLocalDataSource(context))
        ratesRepository = newRepo
        return newRepo
    }

    private fun createRatesLocalDataSource(context: Context): RatesDataSource {
        val database = database ?: createDataBase(context)
        val sharedPreferences = sharedPreferences ?: createSharedPreferences(context)
        return RatesLocalDataSource(database.ratesDao(), sharedPreferences)
    }

    private fun createDataBase(context: Context): RatesDatabase {
        val result = Room.databaseBuilder(
                context.applicationContext,
                RatesDatabase::class.java, "Rates.db"
        ).build()
        database = result
        return result
    }

    private fun createSharedPreferences(context: Context): SharedPreferences =
            context?.getSharedPreferences(context.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE)


    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            ratesRepository = null
            sharedPreferences = null
        }
    }

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }
}
