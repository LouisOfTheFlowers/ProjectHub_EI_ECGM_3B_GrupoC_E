package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncAction(syncAction: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE synced = 0")
    suspend fun getPendingSyncActions(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Delete
    suspend fun deleteSyncAction(syncAction: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE synced = 1")
    suspend fun deleteSyncedActions()

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAllSyncActions()
}