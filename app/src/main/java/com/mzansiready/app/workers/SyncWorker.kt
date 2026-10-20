package com.mzansiready.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mzansiready.app.MzansiApp

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting background sync")
            val app = applicationContext as MzansiApp
            val result = app.locationRepository.syncPending()

            if (result.isSuccess) {
                Log.d("SyncWorker", "Sync completed successfully")
                Result.success()
            } else {
                Log.w("SyncWorker", "Sync failed, will retry")
                Result.retry()
            }
        } catch (t: Throwable) {
            Log.e("SyncWorker", "Sync crashed", t)
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "mzansi_sync_worker"
    }
}

