package com.meteohealth.domain.model

data class Profile(
    val id: Long = 1,
    val name: String = "",
    val age: Int = 0,
    val sensitivity: Int = 3,
    val healthConditions: List<String> = emptyList(),
    val city: String = "Москва",
    val lat: Double = 55.7558,
    val lon: Double = 37.6176,
    val autoDetectLocation: Boolean = true,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val notificationPrefs: Map<String, Boolean> = emptyMap(),
)
