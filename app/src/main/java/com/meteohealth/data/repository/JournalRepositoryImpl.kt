package com.meteohealth.data.repository

import com.meteohealth.data.storage.dao.JournalDao
import com.meteohealth.data.storage.entity.JournalEntryEntity
import com.meteohealth.data.storage.entity.JournalEntryMetricEntity
import com.meteohealth.data.storage.entity.JournalEntrySymptomEntity
import com.meteohealth.data.storage.entity.SymptomEntity
import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.model.JournalEntry
import com.meteohealth.domain.model.Symptom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl(private val dao: JournalDao) : JournalGateway {

    override fun observeAll(): Flow<List<JournalEntry>> =
        dao.observeAll().map { entries ->
            entries.map { entity ->
                val symptomNames = dao.getSymptomsForEntry(entity.id)
                val metrics = dao.getMetricsForEntry(entity.id)
                JournalEntry(
                    id = entity.id,
                    ts = entity.ts,
                    level = entity.level,
                    notes = entity.notes,
                    symptoms = symptomNames.mapNotNull { name ->
                        runCatching { Symptom.valueOf(name) }.getOrNull()
                    },
                    metrics = metrics.associate { it.metric to it.value },
                )
            }
        }

    override suspend fun append(entry: JournalEntry) {
        val entryId = dao.upsertEntry(
            JournalEntryEntity(id = entry.id, ts = entry.ts, level = entry.level, notes = entry.notes)
        )
        val symptomLinks = entry.symptoms.map { symptom ->
            val existing = dao.findSymptomByName(symptom.name)
            val symptomId = existing?.id ?: dao.upsertSymptom(SymptomEntity(name = symptom.name))
            JournalEntrySymptomEntity(entryId = entryId, symptomId = symptomId)
        }
        dao.upsertEntrySymptoms(symptomLinks)
        dao.upsertMetrics(entry.metrics.map { (key, value) ->
            JournalEntryMetricEntity(entryId = entryId, metric = key, value = value)
        })
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
