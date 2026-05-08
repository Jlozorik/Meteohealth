package com.meteohealth.data.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meteohealth.data.storage.entity.NotificationLogEntity

@Dao
interface NotificationLogDao {
    @Insert
    suspend fun insert(entity: NotificationLogEntity)

    @Query("DELETE FROM notification_log WHERE ts < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long)
}
