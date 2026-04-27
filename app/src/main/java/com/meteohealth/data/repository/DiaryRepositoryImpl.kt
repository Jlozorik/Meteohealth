package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.DiaryEntryDao
import com.meteohealth.data.local.entity.DiaryEntryEntity
import com.meteohealth.domain.model.DiaryEntry
import com.meteohealth.domain.model.WellbeingLevel
import com.meteohealth.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiaryRepositoryImpl(
    private val dao: DiaryEntryDao
) : DiaryRepository {

    override fun observeAll(): Flow<List<DiaryEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRange(from: Long, to: Long): Flow<List<DiaryEntry>> =
        dao.observeRange(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun save(entry: DiaryEntry): Long =
        dao.insert(entry.toEntity())

    override suspend fun delete(entry: DiaryEntry) =
        dao.delete(entry.toEntity())

    override suspend fun clearAll() = dao.deleteAll()

    private fun DiaryEntryEntity.toDomain() = DiaryEntry(
        id = id,
        timestamp = timestamp,
        wellbeingLevel = runCatching { WellbeingLevel.valueOf(wellbeingLevel) }
            .getOrDefault(WellbeingLevel.FAIR),
        symptoms = symptoms,
        notes = notes,
        temperatureCelsius = temperatureCelsius,
        pressureHpa = pressureHpa,
        kpIndex = kpIndex
    )

    private fun DiaryEntry.toEntity() = DiaryEntryEntity(
        id = id,
        timestamp = timestamp,
        wellbeingLevel = wellbeingLevel.name,
        symptoms = symptoms,
        notes = notes,
        temperatureCelsius = temperatureCelsius,
        pressureHpa = pressureHpa,
        kpIndex = kpIndex
    )
}
