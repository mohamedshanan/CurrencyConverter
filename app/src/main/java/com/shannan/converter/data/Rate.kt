package com.shannan.converter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class Rate @JvmOverloads constructor(
        @PrimaryKey @ColumnInfo(name = "currency") var currency: String = "EUR",
        @ColumnInfo(name = "rate") var value: Double = 1.0
)