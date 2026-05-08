package com.meteohealth.data.repository

import com.meteohealth.data.storage.dao.ProfileDao
import com.meteohealth.data.storage.entity.DisplayPrefEntity
import com.meteohealth.data.storage.entity.HealthConditionEntity
import com.meteohealth.data.storage.entity.LocationEntity
import com.meteohealth.data.storage.entity.NotificationPrefEntity
import com.meteohealth.data.storage.entity.ProfileEntity
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProfileRepositoryImpl(private val dao: ProfileDao) : ProfileGateway {

    override fun observe(): Flow<Profile?> = combine(
        dao.observe(),
        dao.observeLocation(),
        dao.observeConditions(),
        dao.observeNotificationPrefs(),
        dao.observeDisplayPref(),
    ) { profile, location, conditions, notifPrefs, displayPref ->
        profile ?: return@combine null
        Profile(
            id = profile.id,
            name = profile.name,
            age = profile.age,
            sensitivity = profile.sensitivity,
            healthConditions = conditions.map { it.condition },
            city = location?.city ?: "",
            lat = location?.lat ?: 0.0,
            lon = location?.lon ?: 0.0,
            autoDetectLocation = (location?.autoDetect ?: 1) == 1,
            pressureUnit = runCatching { PressureUnit.valueOf(displayPref?.pressureUnit ?: "") }
                .getOrDefault(PressureUnit.HPA),
            notificationPrefs = notifPrefs.associate { it.kind to (it.enabled == 1) },
        )
    }

    override suspend fun save(profile: Profile) {
        dao.upsertProfile(ProfileEntity(
            id = profile.id, name = profile.name, age = profile.age, sensitivity = profile.sensitivity
        ))
        dao.upsertLocation(LocationEntity(
            profileId = profile.id,
            city = profile.city,
            lat = profile.lat,
            lon = profile.lon,
            autoDetect = if (profile.autoDetectLocation) 1 else 0,
        ))
        dao.deleteConditions()
        dao.upsertConditions(profile.healthConditions.map {
            HealthConditionEntity(profileId = profile.id, condition = it)
        })
        dao.deleteNotificationPrefs()
        dao.upsertNotificationPrefs(profile.notificationPrefs.map { (kind, enabled) ->
            NotificationPrefEntity(profileId = profile.id, kind = kind, enabled = if (enabled) 1 else 0)
        })
        dao.upsertDisplayPref(DisplayPrefEntity(
            profileId = profile.id, pressureUnit = profile.pressureUnit.name
        ))
    }
}
