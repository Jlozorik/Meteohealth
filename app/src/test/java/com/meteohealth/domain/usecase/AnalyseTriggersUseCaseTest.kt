package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.JournalEntry
import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.WeatherHour
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyseTriggersUseCaseTest {

    private val journalGateway = mockk<JournalGateway>()
    private val weatherGateway = mockk<WeatherGateway>()
    private val kpGateway = mockk<KpGateway>()

    private val useCase = AnalyseTriggersUseCase(journalGateway, weatherGateway, kpGateway)

    private fun makeWeather(epoch: Long, pressure: Double, temp: Double) = WeatherHour(
        hourBucketEpoch = epoch,
        tempC = temp,
        pressureHpa = pressure,
        humidity = 60,
        windMps = 2.0,
        description = "",
        icon = "",
        city = "",
    )

    @Test
    fun `returns empty list when journal is empty`() = runTest {
        every { journalGateway.observeAll() } returns flowOf(emptyList())
        every { weatherGateway.observeHistory(any()) } returns flowOf(emptyList())
        every { kpGateway.observeHistory(any()) } returns flowOf(emptyList())

        val results = useCase().first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `returns three trigger results for pressure, kp, and temp`() = runTest {
        val entries = (1..14).map { i ->
            JournalEntry(ts = i.toLong() * 3600_000L, level = i * 5)
        }
        val weather = (1..14).map { i ->
            makeWeather(epoch = i.toLong(), pressure = 1010.0 + i, temp = 10.0 + i)
        }
        val kpSamples = (1..14).map { i -> KpSample(ts = i.toLong() * 3600_000L, kp = i * 0.3) }

        every { journalGateway.observeAll() } returns flowOf(entries)
        every { weatherGateway.observeHistory(any()) } returns flowOf(weather)
        every { kpGateway.observeHistory(any()) } returns flowOf(kpSamples)

        val results = useCase().first()

        assertEquals(3, results.size)
        val keys = results.map { it.factor }.toSet()
        assertTrue("pressure" in keys)
        assertTrue("kp" in keys)
        assertTrue("temp" in keys)
    }

    @Test
    fun `correlation coefficient is in valid range`() = runTest {
        val entries = (1..20).map { i ->
            JournalEntry(ts = i.toLong() * 3600_000L, level = i * 4)
        }
        val weather = (1..20).map { i ->
            makeWeather(epoch = i.toLong(), pressure = 1000.0 + i * 2, temp = 5.0 + i)
        }
        val kpSamples = (1..20).map { i -> KpSample(ts = i.toLong() * 3600_000L, kp = 1.0) }

        every { journalGateway.observeAll() } returns flowOf(entries)
        every { weatherGateway.observeHistory(any()) } returns flowOf(weather)
        every { kpGateway.observeHistory(any()) } returns flowOf(kpSamples)

        val results = useCase().first()

        results.forEach { r ->
            assertTrue("r must be in [-1, 1]: ${r.r}", r.r in -1.0..1.0)
        }
    }
}
