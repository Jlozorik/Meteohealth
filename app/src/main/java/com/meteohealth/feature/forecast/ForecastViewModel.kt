package com.meteohealth.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.RiskLevel
import com.meteohealth.domain.model.WeatherHour
import com.meteohealth.domain.usecase.ObserveForecastUseCase
import com.meteohealth.domain.usecase.ObserveProfileUseCase
import com.meteohealth.domain.wellbeing.RiskClassifier
import com.meteohealth.domain.wellbeing.WellbeingInput
import com.meteohealth.domain.wellbeing.WellbeingPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ForecastViewModel(
    private val observeForecast: ObserveForecastUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val pipeline: WellbeingPipeline,
) : ViewModel() {

    private val _state = MutableStateFlow(ForecastState())
    val state: StateFlow<ForecastState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("EE, dd.MM", Locale.getDefault())

    init {
        viewModelScope.launch {
            observeProfile().collect { profile ->
                val unit = profile?.pressureUnit ?: PressureUnit.HPA
                _state.value = _state.value.copy(pressureUnit = unit)
            }
        }
        viewModelScope.launch {
            observeForecast()
                .catch { e -> dispatch(ForecastIntent.ErrorOccurred(e.message ?: "Ошибка")) }
                .collect { feed ->
                    val days = buildDayRows(feed.hours, feed.kpSamples.map { it.kp })
                    dispatch(ForecastIntent.FeedArrived(days, feed.kpSamples))
                }
        }
    }

    fun dispatch(intent: ForecastIntent) {
        val (newState, _) = ForecastReducer.reduce(_state.value, intent)
        _state.value = newState
    }

    private fun buildDayRows(hours: List<WeatherHour>, kpValues: List<Double>): List<DayRow> {
        val byDay = hours.groupBy { it.hourBucketEpoch / 24 }
        val avgKp = if (kpValues.isEmpty()) 0.0 else kpValues.average()
        return byDay.entries.sortedBy { it.key }.take(5).map { (_, dayHours) ->
            val temps = dayHours.map { it.tempC }
            val pressures = dayHours.map { it.pressureHpa }
            val avgP = pressures.average()
            val pDelta = if (pressures.size >= 2) pressures.last() - pressures.first() else 0.0
            val input = WellbeingInput(
                pressureDelta6h = pDelta,
                kpIndex = avgKp,
                tempDelta24h = 0.0,
                humidity = dayHours.map { it.humidity.toDouble() }.average(),
                profile = com.meteohealth.domain.model.Profile(),
            )
            val score = pipeline.compute(input).score
            val risk = RiskClassifier.classify(score)
            DayRow(
                dateLabel = dateFormat.format(Date(dayHours.first().hourBucketEpoch * 3_600_000L)),
                tempMin = temps.minOrNull() ?: 0.0,
                tempMax = temps.maxOrNull() ?: 0.0,
                pressureHpa = avgP,
                kp = avgKp,
                riskLabel = riskLabel(risk),
                hours = dayHours,
            )
        }
    }

    private fun riskLabel(risk: RiskLevel) = when (risk) {
        RiskLevel.CALM  -> "отсутствует"
        RiskLevel.WATCH -> "небольшой"
        RiskLevel.ALERT -> "средний"
        RiskLevel.HIGH  -> "высокий"
    }
}
