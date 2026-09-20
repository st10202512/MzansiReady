package com.mzansiready.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("daily") val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("precipitation") val precipitation: Double
)

data class DailyWeather(
    @SerializedName("time") val dates: List<String>,
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double>,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double>,
    @SerializedName("precipitation_probability_max") val rainProbabilities: List<Int>
)