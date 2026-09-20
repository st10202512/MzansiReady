package com.mzansiready.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodesTest {

    @Test
    fun clearSky_describesCorrectly() {
        assertEquals("Clear sky", WeatherCodes.describe(0))
    }

    @Test
    fun rain_describesCorrectly() {
        assertEquals("Rain", WeatherCodes.describe(61))
    }

    @Test
    fun thunderstorm_describesCorrectly() {
        assertEquals("Thunderstorm", WeatherCodes.describe(95))
    }

    @Test
    fun unknownCode_returnsUnknown() {
        assertEquals("Unknown conditions", WeatherCodes.describe(999))
    }
}