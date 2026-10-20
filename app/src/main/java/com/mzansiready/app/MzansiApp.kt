package com.mzansiready.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mzansiready.app.data.local.AppDatabase
import com.mzansiready.app.data.remote.RetrofitClient
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.data.repo.LocationRepository
import com.mzansiready.app.data.repo.WeatherRepository
import com.mzansiready.app.util.NetworkMonitor
import com.mzansiready.app.util.SessionManager
import com.mzansiready.app.workers.SyncWorker
import java.util.concurrent.TimeUnit

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

    override fun onCreate() {
        super.onCreate()
        scheduleSyncWorker()
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    companion object {
        fun from(context: android.content.Context): MzansiApp =
            context.applicationContext as MzansiApp
    }
}