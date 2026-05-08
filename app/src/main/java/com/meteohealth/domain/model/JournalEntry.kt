package com.meteohealth.domain.model

data class JournalEntry(
    val id: Long = 0,
    val ts: Long,
    val level: Int,
    val notes: String = "",
    val symptoms: List<Symptom> = emptyList(),
    val metrics: Map<String, Double> = emptyMap(),
)
