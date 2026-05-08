package com.meteohealth.data.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meteohealth.data.storage.entity.WeatherHourEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_hour ORDER BY hour_bucket_epoch DESC LIMIT 1")
    fun observeLatest(): Flow<WeatherHourEntity?>

    @Query("SELECT * FROM weather_hour WHERE hour_bucket_epoch >= :sinceEpoch ORDER BY hour_bucket_epoch ASC")
    fun observeHistory(sinceEpoch: Long): Flow<List<WeatherHourEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<WeatherHourEntity>)

    @Query("DELETE FROM weather_hour WHERE hour_bucket_epoch < :epochSeconds")
    suspend fun deleteOlderThan(epochSeconds: Long)
}
