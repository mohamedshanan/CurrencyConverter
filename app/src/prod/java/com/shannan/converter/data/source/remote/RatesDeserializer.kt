package com.shannan.converter.data.source.remote

import com.google.gson.*
import com.shannan.converter.data.Rate
import com.shannan.converter.data.RatesResponse
import java.lang.reflect.Type

class RatesDeserializer : JsonDeserializer<RatesResponse> {

    override fun deserialize(response: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RatesResponse? {
        if (response == null)
            return null
        val responseJson = response.asJsonObject
        val ratesList = mutableListOf<Rate>()
        val rates = responseJson.get("rates")?.asJsonObject
        rates?.entrySet()?.forEach{ rate ->
            ratesList.add(Rate(rate.key, rate.value.asDouble))
        }
        return RatesResponse(responseJson.get("timestamp").asLong,
                responseJson.get("base").asString,
                responseJson.get("date").asString,
                ratesList)
    }
}