package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.Profile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshNowUseCaseTest {

    private val weatherGateway = mockk<WeatherGateway>()
    private val kpGateway = mockk<KpGateway>()
    private val profileGateway = mockk<ProfileGateway>()

    private val useCase = RefreshNowUseCase(weatherGateway, kpGateway, profileGateway)

    @Test
    fun `refreshes both weather and kp`() = runTest {
        every { profileGateway.observe() } returns flowOf(Profile(lat = 55.75, lon = 37.62))
        coEvery { weatherGateway.refresh(any(), any()) } just runs
        coEvery { kpGateway.refresh() } just runs

        useCase()

        coVerify { weatherGateway.refresh(any(), any()) }
        coVerify { kpGateway.refresh() }
    }

    @Test
    fun `uses default coords when profile has no location`() = runTest {
        every { profileGateway.observe() } returns flowOf(null)
        coEvery { weatherGateway.refresh(0.0, 0.0) } just runs
        coEvery { kpGateway.refresh() } just runs

        useCase()

        coVerify { weatherGateway.refresh(0.0, 0.0) }
    }
}

