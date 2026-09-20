package com.mzansiready.app.util

object WeatherCodes {

    fun describe(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snowfall"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown conditions"
    }

    fun emoji(code: Int): String = when (code) {
        0, 1 -> "\u2600\uFE0F"
        2, 3 -> "\u26C5"
        45, 48 -> "\uD83C\uDF2B\uFE0F"
        in 51..67 -> "\uD83C\uDF27\uFE0F"
        in 71..77 -> "\u2744\uFE0F"
        in 80..86 -> "\uD83C\uDF26\uFE0F"
        95, 96, 99 -> "\u26C8\uFE0F"
        else -> "\uD83C\uDF21\uFE0F"
    }
}