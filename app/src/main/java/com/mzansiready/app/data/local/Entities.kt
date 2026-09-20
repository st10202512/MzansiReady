package com.mzansiready.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,
    val locationName: String,
    val temperature: Double,
    val condition: String,
    val weatherCode: Int,
    val minTemperature: Double,
    val maxTemperature: Double,
    val rainProbability: Int,
    val retrievedAt: Long
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean = false,
    val pendingDelete: Boolean = false
)