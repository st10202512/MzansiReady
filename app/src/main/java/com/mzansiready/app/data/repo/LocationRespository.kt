package com.mzansiready.app.data.repo

import android.util.Log
import com.mzansiready.app.data.local.LocationDao
import com.mzansiready.app.data.local.SavedLocationEntity
import com.mzansiready.app.data.remote.ApiService
import com.mzansiready.app.data.remote.dto.CreateLocationRequest
import com.mzansiready.app.data.remote.dto.LocationDto
import com.mzansiready.app.util.NetworkMonitor
import com.mzansiready.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(
    private val api: ApiService,
    private val dao: LocationDao,
    private val session: SessionManager,
    private val network: NetworkMonitor
) {

    companion object {
        private const val TAG = "LocationRepository"
    }

    suspend fun refresh(): Result<List<SavedLocationEntity>> = withContext(Dispatchers.IO) {
        try {
            if (network.isOnline()) {
                Log.d(TAG, "Online: fetching locations from API")
                val response = api.getLocations(session.bearer())
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    dao.clear()
                    body.forEach { dto ->
                        dao.insert(
                            SavedLocationEntity(
                                remoteId = dto.locationId,
                                name = dto.name,
                                latitude = dto.latitude,
                                longitude = dto.longitude,
                                isSynced = true,
                                pendingDelete = false
                            )
                        )
                    }
                }
            } else {
                Log.d(TAG, "Offline: returning cached locations")
            }
            Result.success(dao.getVisible())
        } catch (t: Throwable) {
            Log.e(TAG, "Refresh failed, returning cache", t)
            Result.success(dao.getVisible())
        }
    }

    suspend fun add(name: String, latitude: Double, longitude: Double): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (network.isOnline()) {
                    Log.d(TAG, "Online: adding location to API")
                    val response = api.createLocation(
                        session.bearer(),
                        CreateLocationRequest(name, latitude, longitude)
                    )
                    val body: LocationDto? = response.body()
                    if (response.isSuccessful && body != null) {
                        dao.insert(
                            SavedLocationEntity(
                                remoteId = body.locationId,
                                name = body.name,
                                latitude = body.latitude,
                                longitude = body.longitude,
                                isSynced = true
                            )
                        )
                        return@withContext Result.success(Unit)
                    }
                }

                Log.d(TAG, "Offline: saving location locally for later sync")
                dao.insert(
                    SavedLocationEntity(
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        isSynced = false
                    )
                )
                Result.success(Unit)
            } catch (t: Throwable) {
                Log.e(TAG, "Add failed, saving offline", t)
                dao.insert(
                    SavedLocationEntity(
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        isSynced = false
                    )
                )
                Result.success(Unit)
            }
        }

    suspend fun delete(entity: SavedLocationEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (entity.remoteId != null && network.isOnline()) {
                Log.d(TAG, "Online: deleting from API")
                val response = api.deleteLocation(session.bearer(), entity.remoteId)
                if (response.isSuccessful) {
                    dao.deleteById(entity.id)
                    return@withContext Result.success(Unit)
                }
            }

            if (entity.remoteId == null) {
                dao.deleteById(entity.id)
            } else {
                dao.markForDeletion(entity.id)
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            dao.markForDeletion(entity.id)
            Result.success(Unit)
        }
    }

    suspend fun syncPending(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!network.isOnline()) {
            Log.d(TAG, "Not online, skipping sync")
            return@withContext Result.success(Unit)
        }

        Log.d(TAG, "Starting sync of pending changes")

        try {
            val unsyncedLocations = dao.getUnsyncedLocations()
            Log.d(TAG, "Syncing ${unsyncedLocations.size} unsynced locations")

            unsyncedLocations.forEach { entity ->
                try {
                    val response = api.createLocation(
                        session.bearer(),
                        CreateLocationRequest(entity.name, entity.latitude, entity.longitude)
                    )
                    val body: LocationDto? = response.body()
                    if (response.isSuccessful && body != null) {
                        dao.markAsSynced(entity.id, body.locationId)
                        Log.d(TAG, "Synced location: ${entity.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync location: ${entity.name}", e)
                }
            }

            val pendingDeletes = dao.getPendingDeletes()
            Log.d(TAG, "Syncing ${pendingDeletes.size} pending deletes")

            pendingDeletes.forEach { entity ->
                try {
                    if (entity.remoteId != null) {
                        val response = api.deleteLocation(session.bearer(), entity.remoteId)
                        if (response.isSuccessful) {
                            dao.deleteById(entity.id)
                            Log.d(TAG, "Synced deletion: ${entity.name}")
                        }
                    } else {
                        dao.deleteById(entity.id)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync deletion: ${entity.name}", e)
                }
            }

            Result.success(Unit)
        } catch (t: Throwable) {
            Log.e(TAG, "Sync failed", t)
            Result.failure(t)
        }
    }
}