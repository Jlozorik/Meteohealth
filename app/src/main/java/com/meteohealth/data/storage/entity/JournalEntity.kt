package com.meteohealth.data.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entry")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val level: Int,
    val notes: String,
)

@Entity(tableName = "symptom")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "journal_entry_symptom",
    primaryKeys = ["entry_id", "symptom_id"],
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SymptomEntity::class,
            parentColumns = ["id"],
            childColumns = ["symptom_id"],
        ),
    ],
)
data class JournalEntrySymptomEntity(
    @ColumnInfo(name = "entry_id") val entryId: Long,
    @ColumnInfo(name = "symptom_id") val symptomId: Long,
)

@Entity(
    tableName = "journal_entry_metric",
    primaryKeys = ["entry_id", "metric"],
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class JournalEntryMetricEntity(
    @ColumnInfo(name = "entry_id") val entryId: Long,
    val metric: String,
    val value: Double,
)
