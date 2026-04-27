package com.meteohealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meteohealth.data.local.entity.KpCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KpCacheDao {
    @Query("SELECT * FROM kp_cache ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): KpCacheEntity?

    @Query("SELECT * FROM kp_cache ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 60): Flow<List<KpCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<KpCacheEntity>)

    @Query("DELETE FROM kp_cache WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
