package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.model.JournalEntry
import com.meteohealth.domain.model.Symptom
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppendJournalEntryUseCaseTest {

    private val journalGateway = mockk<JournalGateway>()
    private val useCase = AppendJournalEntryUseCase(journalGateway)

    @Test
    fun `delegates append to gateway`() = runTest {
        val entry = JournalEntry(ts = 1000L, level = 75)
        coEvery { journalGateway.append(entry) } just runs

        useCase(entry)

        coVerify(exactly = 1) { journalGateway.append(entry) }
    }

    @Test
    fun `passes entry with symptoms to gateway unchanged`() = runTest {
        val entry = JournalEntry(
            ts = 2000L,
            level = 50,
            symptoms = listOf(Symptom.HEADACHE, Symptom.FATIGUE),
            notes = "Болела голова с утра.",
        )
        coEvery { journalGateway.append(entry) } just runs

        useCase(entry)

        coVerify { journalGateway.append(entry) }
    }

    @Test
    fun `passes entry with metrics to gateway unchanged`() = runTest {
        val entry = JournalEntry(
            ts = 3000L,
            level = 60,
            metrics = mapOf("pressure_hpa" to 1010.0, "temp_c" to 18.0),
        )
        coEvery { journalGateway.append(entry) } just runs

        useCase(entry)

        coVerify { journalGateway.append(entry) }
    }
}
