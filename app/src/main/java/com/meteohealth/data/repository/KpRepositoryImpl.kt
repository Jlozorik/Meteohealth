package com.meteohealth.data.repository

import com.meteohealth.data.network.service.NoaaSwpcService
import com.meteohealth.data.storage.dao.KpDao
import com.meteohealth.data.storage.entity.KpMinuteEntity
import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.model.KpSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KpRepositoryImpl(
    private val kpDao: KpDao,
    private val service: NoaaSwpcService,
) : KpGateway {

    // Усредняем последние 60 минут, чтобы Kp не скакал каждую минуту
    override fun observeLatest(): Flow<KpSample?> =
        kpDao.observeLastHour().map { entries ->
            if (entries.isEmpty()) null
            else KpSample(ts = entries.first().ts, kp = entries.map { it.kp }.average())
        }

    override fun observeHistory(sinceEpoch: Long): Flow<List<KpSample>> =
        kpDao.observeHistory(sinceEpoch).map { list -> list.map { it.toDomain() } }

    override suspend fun refresh() {
        val entries = service.kpIndex()
        val entities = entries.map { dto ->
            KpMinuteEntity(ts = parseTimeTag(dto.timeTag), kp = dto.kpIndex)
        }
        kpDao.upsertAll(entities)
        val cutoff = System.currentTimeMillis() - 24 * 3600 * 1000L
        kpDao.deleteOlderThan(cutoff)
    }

    private fun KpMinuteEntity.toDomain() = KpSample(ts = ts, kp = kp)

    private fun parseTimeTag(tag: String): Long {
        // "2026-05-08 12:00:00" → epoch millis
        return try {
            val parts = tag.split(" ")
            val date = parts[0].split("-")
            val time = parts[1].split(":")
            val year = date[0].toInt(); val month = date[1].toInt(); val day = date[2].toInt()
            val hour = time[0].toInt(); val min = time[1].toInt()
            @Suppress("DEPRECATION")
            java.util.Date(year - 1900, month - 1, day, hour, min).time
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
