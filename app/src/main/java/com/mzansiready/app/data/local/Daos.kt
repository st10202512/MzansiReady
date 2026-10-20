package com.mzansiready.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherCacheEntity)

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
    suspend fun findByKey(key: String): WeatherCacheEntity?

    @Query("DELETE FROM weather_cache")
    suspend fun clear()
}

@Dao
interface LocationDao {

    @Query("SELECT * FROM saved_locations WHERE pendingDelete = 0 ORDER BY id ASC")
    suspend fun getVisible(): List<SavedLocationEntity>

    @Query("SELECT * FROM saved_locations WHERE isSynced = 0 AND pendingDelete = 0")
    suspend fun getUnsyncedLocations(): List<SavedLocationEntity>

    @Query("SELECT * FROM saved_locations WHERE pendingDelete = 1")
    suspend fun getPendingDeletes(): List<SavedLocationEntity>

    @Insert
    suspend fun insert(entity: SavedLocationEntity): Long

    @Update
    suspend fun update(entity: SavedLocationEntity)

    @Query("UPDATE saved_locations SET isSynced = 1, remoteId = :remoteId WHERE id = :localId")
    suspend fun markAsSynced(localId: Int, remoteId: Int)

    @Query("UPDATE saved_locations SET pendingDelete = 1 WHERE id = :id")
    suspend fun markForDeletion(id: Int)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM saved_locations")
    suspend fun clear()
}