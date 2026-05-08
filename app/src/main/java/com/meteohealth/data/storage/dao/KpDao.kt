package com.meteohealth.data.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meteohealth.data.storage.entity.KpMinuteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KpDao {
    @Query("SELECT * FROM kp_minute ORDER BY ts DESC LIMIT 1")
    fun observeLatestRaw(): Flow<KpMinuteEntity?>

    @Query("SELECT * FROM kp_minute ORDER BY ts DESC LIMIT 60")
    fun observeLastHour(): Flow<List<KpMinuteEntity>>

    @Query("SELECT * FROM kp_minute WHERE ts >= :sinceEpoch ORDER BY ts ASC")
    fun observeHistory(sinceEpoch: Long): Flow<List<KpMinuteEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<KpMinuteEntity>)

    @Query("DELETE FROM kp_minute WHERE ts < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long)
}
