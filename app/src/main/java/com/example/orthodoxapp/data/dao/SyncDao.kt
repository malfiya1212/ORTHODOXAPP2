package com.example.orthodoxapp.data.dao

import androidx.room.*
import com.example.orthodoxapp.data.model.SyncQueue
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToQueue(item: SyncQueue): Long

    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getQueue(): Flow<List<SyncQueue>>

    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getNextInQueue(): SyncQueue?

    @Query("UPDATE sync_queue SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Delete
    suspend fun removeFromQueue(item: SyncQueue)

    @Update
    suspend fun updateQueueItem(item: SyncQueue)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
