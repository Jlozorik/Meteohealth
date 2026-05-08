package com.meteohealth.data.repository

import android.util.Log
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
        dao.observeRecent(1).map { list ->
            list.firstOrNull()?.kpIndex?.takeIf { it >= 0f }
        }

    override suspend fun refreshKp() {
        try {
            val rows = api.getKpData()
            val entities = rows.mapNotNull { dto ->
                val kp = dto.kpIndex?.toFloat() ?: return@mapNotNull null
                if (kp < 0f) return@mapNotNull null  // NOAA uses -1 as "no data"
                val timestamp = parseNoaaTimestamp(dto.timeTag)
                if (timestamp == 0L) return@mapNotNull null
                KpCacheEntity(timestamp = timestamp, kpIndex = kp)
            }
            if (entities.isNotEmpty()) {
                dao.insertAll(entities)
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                dao.deleteOlderThan(cutoff)
            } else {
                Log.w(TAG, "refreshKp: ответ NOAA пуст или не содержит валидных данных (${rows.size} строк)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshKp: ошибка загрузки Kp-индекса", e)
        }
    }

    // NOAA timestamp: "2026-05-08T11:16:00.000" (ISO 8601) или "2024-01-15 12:00:00.000" (старый формат)
    private fun parseNoaaTimestamp(ts: String): Long {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (fmt in formats) {
            try {
                val result = java.text.SimpleDateFormat(fmt, java.util.Locale.US)
                    .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                    .parse(ts)?.time
                if (result != null && result > 0L) return result
            } catch (_: Exception) { /* следующий формат */ }
        }
        return 0L
    }

    companion object {
        private const val TAG = "KpRepository"
    }
}
