package com.meteohealth.domain.repository

import com.meteohealth.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun observeAll(): Flow<List<DiaryEntry>>
    fun observeRange(from: Long, to: Long): Flow<List<DiaryEntry>>
    suspend fun save(entry: DiaryEntry): Long
    suspend fun delete(entry: DiaryEntry)
}
