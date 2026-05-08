package com.meteohealth.data.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.meteohealth.data.storage.entity.DisplayPrefEntity
import com.meteohealth.data.storage.entity.HealthConditionEntity
import com.meteohealth.data.storage.entity.LocationEntity
import com.meteohealth.data.storage.entity.NotificationPrefEntity
import com.meteohealth.data.storage.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun observe(): Flow<ProfileEntity?>

    @Upsert
    suspend fun upsertProfile(entity: ProfileEntity)

    @Upsert
    suspend fun upsertLocation(entity: LocationEntity)

    @Query("SELECT * FROM location WHERE profile_id = 1")
    fun observeLocation(): Flow<LocationEntity?>

    @Query("SELECT * FROM health_conditions WHERE profile_id = 1")
    fun observeConditions(): Flow<List<HealthConditionEntity>>

    @Query("DELETE FROM health_conditions WHERE profile_id = 1")
    suspend fun deleteConditions()

    @Upsert
    suspend fun upsertConditions(entities: List<HealthConditionEntity>)

    @Query("SELECT * FROM notification_pref WHERE profile_id = 1")
    fun observeNotificationPrefs(): Flow<List<NotificationPrefEntity>>

    @Query("DELETE FROM notification_pref WHERE profile_id = 1")
    suspend fun deleteNotificationPrefs()

    @Upsert
    suspend fun upsertNotificationPrefs(entities: List<NotificationPrefEntity>)

    @Upsert
    suspend fun upsertDisplayPref(entity: DisplayPrefEntity)

    @Query("SELECT * FROM display_pref WHERE profile_id = 1")
    fun observeDisplayPref(): Flow<DisplayPrefEntity?>
}
