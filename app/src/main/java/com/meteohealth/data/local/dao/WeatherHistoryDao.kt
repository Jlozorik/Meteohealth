package com.meteohealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meteohealth.data.local.entity.WeatherHistoryEntity

@Dao
interface WeatherHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeatherHistoryEntity)

    @Query("SELECT * FROM weather_history WHERE timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    suspend fun getSince(fromTimestamp: Long): List<WeatherHistoryEntity>

    @Query("DELETE FROM weather_history WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}
