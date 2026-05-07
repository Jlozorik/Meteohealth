package com.meteohealth.data.repository

import com.meteohealth.domain.repository.KpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Демо-реализация [KpRepository]: возвращает стабильное умеренное значение Kp,
 * чтобы карточка геомагнитной активности на дашборде и в прогнозе была видна
 * без реального запроса к NOAA SWPC.
 */
class FakeKpRepository : KpRepository {

    private val demoKp = 3.6f

    override fun observeLatestKp(): Flow<Float?> = flowOf(demoKp)

    override suspend fun refreshKp() = Unit
}
