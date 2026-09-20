package com.mzansiready.app.data.repo

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

    suspend fun refresh(): Result<List<SavedLocationEntity>> = withContext(Dispatchers.IO) {
        try {
            if (network.isOnline()) {
                val response = api.getLocations(session.bearer())
                if (response.isSuccessful && response.body() != null) {
                    dao.clear()
                    response.body()!!.forEach { dto ->
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
            }
            Result.success(dao.getVisible())
        } catch (t: Throwable) {
            Result.success(dao.getVisible())
        }
    }

    suspend fun add(name: String, latitude: Double, longitude: Double): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (network.isOnline()) {
                    val response = api.createLocation(
                        session.bearer(),
                        CreateLocationRequest(name, latitude, longitude)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val dto: LocationDto = response.body()!!
                        dao.insert(
                            SavedLocationEntity(
                                remoteId = dto.locationId,
                                name = dto.name,
                                latitude = dto.latitude,
                                longitude = dto.longitude,
                                isSynced = true
                            )
                        )
                        return@withContext Result.success(Unit)
                    }
                }
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
                val response = api.deleteLocation(session.bearer(), entity.remoteId)
                if (response.isSuccessful) {
                    dao.deleteById(entity.id)
                    return@withContext Result.success(Unit)
                }
            }
            if (entity.remoteId == null) {
                dao.deleteById(entity.id)
            } else {
                dao.update(entity.copy(pendingDelete = true))
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            dao.update(entity.copy(pendingDelete = true))
            Result.success(Unit)
        }
    }

    suspend fun syncPending(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!network.isOnline()) return@withContext Result.success(Unit)
        try {
            dao.getPending().forEach { entity ->
                if (entity.pendingDelete && entity.remoteId != null) {
                    api.deleteLocation(session.bearer(), entity.remoteId)
                    dao.deleteById(entity.id)
                } else if (!entity.isSynced && entity.remoteId == null) {
                    val response = api.createLocation(
                        session.bearer(),
                        CreateLocationRequest(entity.name, entity.latitude, entity.longitude)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        dao.deleteById(entity.id)
                        dao.insert(
                            entity.copy(
                                id = 0,
                                remoteId = response.body()!!.locationId,
                                isSynced = true
                            )
                        )
                    }
                }
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}