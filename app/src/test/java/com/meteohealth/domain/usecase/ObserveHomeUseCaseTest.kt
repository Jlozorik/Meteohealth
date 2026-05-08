package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.gateway.RecommendationGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.Profile
import com.meteohealth.domain.model.RiskLevel
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.WeatherHour
import com.meteohealth.domain.wellbeing.WellbeingPipeline
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveHomeUseCaseTest {

    private val weatherGateway = mockk<WeatherGateway>()
    private val kpGateway = mockk<KpGateway>()
    private val profileGateway = mockk<ProfileGateway>()
    private val recommendationGateway = mockk<RecommendationGateway>()
    private val pipeline = WellbeingPipeline.default()

    private lateinit var useCase: ObserveHomeUseCase

    private val defaultWeather = WeatherHour(
        hourBucketEpoch = 1000L,
        tempC = 15.0,
        pressureHpa = 1013.0,
        humidity = 60,
        windMps = 3.0,
        description = "Clear",
        icon = "01d",
        city = "Moscow",
    )

    @Before
    fun setUp() {
        useCase = ObserveHomeUseCase(
            weatherGateway, kpGateway, profileGateway, recommendationGateway, pipeline
        )
    }

    @Test
    fun `emits HomeFeed with weather and kp from gateways`() = runTest {
        val kp = KpSample(ts = 1000L, kp = 2.0)
        every { weatherGateway.observeLatest() } returns flowOf(defaultWeather)
        every { weatherGateway.observeHistory(any()) } returns flowOf(emptyList())
        every { kpGateway.observeLatest() } returns flowOf(kp)
        every { profileGateway.observe() } returns flowOf(Profile())
        every { recommendationGateway.get(any()) } returns emptyList()

        val feed = useCase().first()

        assertEquals(defaultWeather, feed.weather)
        assertEquals(kp, feed.kp)
    }

    @Test
    fun `score is 100 when all factors are benign`() = runTest {
        every { weatherGateway.observeLatest() } returns flowOf(defaultWeather)
        every { weatherGateway.observeHistory(any()) } returns flowOf(emptyList())
        every { kpGateway.observeLatest() } returns flowOf(KpSample(0L, 0.0))
        every { profileGateway.observe() } returns flowOf(Profile())
        every { recommendationGateway.get(any()) } returns emptyList()

        val feed = useCase().first()

        assertEquals(100, feed.wellbeing.score)
    }

    @Test
    fun `recommendations are fetched for classified risk level`() = runTest {
        val recs = listOf(Recommendation("r1", "Пей воду."), Recommendation("r2", "Отдохни."))
        every { weatherGateway.observeLatest() } returns flowOf(defaultWeather)
        every { weatherGateway.observeHistory(any()) } returns flowOf(emptyList())
        every { kpGateway.observeLatest() } returns flowOf(KpSample(0L, 0.0))
        every { profileGateway.observe() } returns flowOf(Profile())
        every { recommendationGateway.get(RiskLevel.CALM) } returns recs

        val feed = useCase().first()

        assertEquals(recs, feed.recommendations)
        verify { recommendationGateway.get(RiskLevel.CALM) }
    }

    @Test
    fun `pressure delta is computed from history`() = runTest {
        val historyEntry = defaultWeather.copy(hourBucketEpoch = 800L, pressureHpa = 1008.0)
        every { weatherGateway.observeLatest() } returns flowOf(defaultWeather)
        every { weatherGateway.observeHistory(any()) } returns flowOf(listOf(historyEntry, historyEntry))
        every { kpGateway.observeLatest() } returns flowOf(KpSample(0L, 0.0))
        every { profileGateway.observe() } returns flowOf(Profile())
        every { recommendationGateway.get(any()) } returns emptyList()

        val feed = useCase().first()

        assertEquals(5.0, feed.pressureDelta6h, 0.001)
    }

    @Test
    fun `null weather and kp do not crash — defaults used`() = runTest {
        every { weatherGateway.observeLatest() } returns flowOf(null)
        every { weatherGateway.observeHistory(any()) } returns flowOf(emptyList())
        every { kpGateway.observeLatest() } returns flowOf(null)
        every { profileGateway.observe() } returns flowOf(null)
        every { recommendationGateway.get(any()) } returns emptyList()

        val feed = useCase().first()

        assertNotNull(feed)
        assertTrue(feed.wellbeing.score in 0..100)
    }
}
