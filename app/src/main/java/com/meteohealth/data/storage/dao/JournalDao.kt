package com.meteohealth.data.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meteohealth.data.storage.entity.JournalEntryEntity
import com.meteohealth.data.storage.entity.JournalEntryMetricEntity
import com.meteohealth.data.storage.entity.JournalEntrySymptomEntity
import com.meteohealth.data.storage.entity.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entry ORDER BY ts DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Upsert
    suspend fun upsertEntry(entity: JournalEntryEntity): Long

    @Query("DELETE FROM journal_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM journal_entry")
    suspend fun deleteAll()

    @Query("SELECT * FROM symptom WHERE name = :name LIMIT 1")
    suspend fun findSymptomByName(name: String): SymptomEntity?

    @Upsert
    suspend fun upsertSymptom(entity: SymptomEntity): Long

    @Upsert
    suspend fun upsertEntrySymptoms(entities: List<JournalEntrySymptomEntity>)

    @Query("SELECT s.name FROM symptom s INNER JOIN journal_entry_symptom jes ON jes.symptom_id = s.id WHERE jes.entry_id = :entryId")
    suspend fun getSymptomsForEntry(entryId: Long): List<String>

    @Upsert
    suspend fun upsertMetrics(entities: List<JournalEntryMetricEntity>)

    @Query("SELECT * FROM journal_entry_metric WHERE entry_id = :entryId")
    suspend fun getMetricsForEntry(entryId: Long): List<JournalEntryMetricEntity>
}
