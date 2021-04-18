package com.shannan.converter.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(): Date {
    var inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var date: Date
    try {
        date = inputFormat.parse(this)
    } catch (ex: ParseException) {
        ex.printStackTrace()
        inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        date = inputFormat.parse(this)
    }
    return date
}