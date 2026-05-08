package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.triggers.PearsonAnalyzer
import com.meteohealth.domain.triggers.TriggerResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AnalyseTriggersUseCase(
    private val journalGateway: JournalGateway,
    private val weatherGateway: WeatherGateway,
    private val kpGateway: KpGateway,
) {
    operator fun invoke(): Flow<List<TriggerResult>> = combine(
        journalGateway.observeAll(),
        weatherGateway.observeHistory(0L),
        kpGateway.observeHistory(0L),
    ) { entries, weather, kpSamples ->
        if (entries.isEmpty()) return@combine emptyList()

        val levels = entries.map { it.level.toDouble() }

        val pressures = entries.map { e ->
            weather.minByOrNull { kotlin.math.abs(it.hourBucketEpoch - e.ts / 3600) }
                ?.pressureHpa ?: 0.0
        }
        val kpValues = entries.map { e ->
            kpSamples.minByOrNull { kotlin.math.abs(it.ts - e.ts) }?.kp ?: 0.0
        }
        val temps = entries.map { e ->
            weather.minByOrNull { kotlin.math.abs(it.hourBucketEpoch - e.ts / 3600) }
                ?.tempC ?: 0.0
        }

        listOf(
            PearsonAnalyzer.analyze(levels, pressures, "pressure"),
            PearsonAnalyzer.analyze(levels, kpValues, "kp"),
            PearsonAnalyzer.analyze(levels, temps, "temp"),
        )
    }
}
