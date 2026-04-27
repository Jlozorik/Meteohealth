package com.meteohealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meteohealth.data.local.entity.NotificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_log ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NotificationLogEntity)

    @Query("DELETE FROM notification_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
