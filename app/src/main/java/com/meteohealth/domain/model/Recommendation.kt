package com.meteohealth.domain.model

data class Recommendation(
    val id: String,
    val triggerCondition: String,
    val title: String,
    val text: String,
    val targetGroups: List<String>
)
