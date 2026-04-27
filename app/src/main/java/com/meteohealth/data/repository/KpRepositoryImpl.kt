package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.KpCacheDao
import com.meteohealth.data.local.entity.KpCacheEntity
import com.meteohealth.data.remote.api.KpApi
import com.meteohealth.domain.repository.KpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KpRepositoryImpl(
    private val api: KpApi,
    private val dao: KpCacheDao
) : KpRepository {

    override fun observeLatestKp(): Flow<Float?> =
        dao.observeRecent(1).map { it.firstOrNull()?.kpIndex }

    override suspend fun refreshKp() {
        try {
            val rows = api.getKpData()
            val entities = rows.mapNotNull { row ->
                val ts = row.getOrNull(0) ?: return@mapNotNull null
                val kp = row.getOrNull(1)?.toFloatOrNull() ?: return@mapNotNull null
                KpCacheEntity(
                    timestamp = parseNoaaTimestamp(ts),
                    kpIndex = kp
                )
            }
            if (entities.isNotEmpty()) {
                dao.insertAll(entities)
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                dao.deleteOlderThan(cutoff)
            }
        } catch (_: Exception) {
            // offline-first: используем кэш
        }
    }

    // NOAA timestamp format: "2024-01-15 12:00:00.000"
    private fun parseNoaaTimestamp(ts: String): Long {
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(ts)?.time ?: 0L
        } catch (_: Exception) { 0L }
    }
}
