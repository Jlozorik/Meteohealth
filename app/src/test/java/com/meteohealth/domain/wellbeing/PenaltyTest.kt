package com.meteohealth.domain.wellbeing

import com.meteohealth.domain.model.Profile
import org.junit.Assert.assertEquals
import org.junit.Test

class PenaltyTest {

    private val noProfile = Profile()

    private fun input(
        pressureDelta6h: Double = 0.0,
        kpIndex: Double = 0.0,
        tempDelta24h: Double = 0.0,
        humidity: Double = 50.0,
        profile: Profile = noProfile,
    ) = WellbeingInput(pressureDelta6h, kpIndex, tempDelta24h, humidity, profile)

    @Test fun pressurePenalty_zero_when_no_change() =
        assertEquals(0, PressurePenalty.apply(input(pressureDelta6h = 0.0)))

    @Test fun pressurePenalty_clamps_at_30() =
        assertEquals(30, PressurePenalty.apply(input(pressureDelta6h = 20.0)))

    @Test fun pressurePenalty_uses_absolute_value() =
        assertEquals(PressurePenalty.apply(input(pressureDelta6h = 5.0)),
                     PressurePenalty.apply(input(pressureDelta6h = -5.0)))

    @Test fun pressurePenalty_typical_7hpa() =
        assertEquals(28, PressurePenalty.apply(input(pressureDelta6h = 7.0)))

    @Test fun kpPenalty_zero_when_kp_below_3() =
        assertEquals(0, KpPenalty.apply(input(kpIndex = 1.0)))

    @Test fun kpPenalty_clamps_at_30() =
        assertEquals(30, KpPenalty.apply(input(kpIndex = 10.0)))

    @Test fun kpPenalty_kp_4_5() =
        assertEquals(12, KpPenalty.apply(input(kpIndex = 4.5)))

    @Test fun tempPenalty_zero_when_delta_below_5() =
        assertEquals(0, TempPenalty.apply(input(tempDelta24h = 3.0)))

    @Test fun tempPenalty_clamps_at_20() =
        assertEquals(20, TempPenalty.apply(input(tempDelta24h = 30.0)))

    @Test fun tempPenalty_delta_10_gives_10() =
        assertEquals(10, TempPenalty.apply(input(tempDelta24h = 10.0)))

    @Test fun humidityPenalty_zero_when_below_70() =
        assertEquals(0, HumidityPenalty.apply(input(humidity = 60.0)))

    @Test fun humidityPenalty_clamps_at_10() =
        assertEquals(10, HumidityPenalty.apply(input(humidity = 100.0)))

    @Test fun humidityPenalty_80_percent_gives_5() =
        assertEquals(5, HumidityPenalty.apply(input(humidity = 80.0)))

    @Test fun personalPenalty_neutral_sensitivity() =
        assertEquals(0, PersonalPenalty.apply(input(profile = Profile(sensitivity = 3))))

    @Test fun personalPenalty_high_sensitivity_adds_bonus() {
        val result = PersonalPenalty.apply(input(profile = Profile(sensitivity = 5)))
        assert(result > 0)
    }

    @Test fun personalPenalty_capped_at_10() {
        val p = Profile(sensitivity = 5, healthConditions = List(10) { "cond$it" })
        assert(PersonalPenalty.apply(input(profile = p)) <= 10)
    }
}
