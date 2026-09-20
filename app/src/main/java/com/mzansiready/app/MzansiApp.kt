package com.mzansiready.app

import android.app.Application
import android.content.Context
import com.mzansiready.app.data.local.AppDatabase
import com.mzansiready.app.data.remote.RetrofitClient
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.data.repo.LocationRepository
import com.mzansiready.app.data.repo.WeatherRepository
import com.mzansiready.app.util.NetworkMonitor
import com.mzansiready.app.util.SessionManager

class MzansiApp : Application() {

    val session by lazy { SessionManager(this) }
    val network by lazy { NetworkMonitor(this) }
    val database by lazy { AppDatabase.getInstance(this) }

    val authRepository by lazy { AuthRepository(RetrofitClient.api, session) }
    val locationRepository by lazy {
        LocationRepository(RetrofitClient.api, database.locationDao(), session, network)
    }
    val weatherRepository by lazy {
        WeatherRepository(RetrofitClient.weather, database, network)
    }

    companion object {
        fun from(context: Context): MzansiApp =
            context.applicationContext as MzansiApp
    }
}