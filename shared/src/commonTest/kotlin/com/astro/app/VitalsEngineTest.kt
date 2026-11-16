package com.astro.app

import com.astro.app.scoring.ReadinessScoring
import com.astro.app.vitals.AlertLevel
import com.astro.app.vitals.HealthParameter
import com.astro.app.vitals.ManualInputs
import com.astro.app.vitals.ParameterReading
import com.astro.app.vitals.Thresholds
import com.astro.app.vitals.TimedValue
import com.astro.app.vitals.Triage
import com.astro.app.vitals.Vitals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VitalsEngineTest {

    @Test
    fun hrThresholds() {
        assertEquals(AlertLevel.CRITICAL, Thresholds.hr(49))
        assertEquals(AlertLevel.WARNING, Thresholds.hr(50))
        assertEquals(AlertLevel.NOMINAL, Thresholds.hr(60))
        assertEquals(AlertLevel.NOMINAL, Thresholds.hr(100))
        assertEquals(AlertLevel.WARNING, Thresholds.hr(120))
        assertEquals(AlertLevel.CRITICAL, Thresholds.hr(121))
    }

    @Test
    fun spo2Thresholds() {
        assertEquals(AlertLevel.CRITICAL, Thresholds.spo2(89))
        assertEquals(AlertLevel.WARNING, Thresholds.spo2(90))
        assertEquals(AlertLevel.WARNING, Thresholds.spo2(94))
        assertEquals(AlertLevel.NOMINAL, Thresholds.spo2(95))
    }

    @Test
    fun glucoseThresholds() {
        assertEquals(AlertLevel.CRITICAL, Thresholds.glucose(53))
        assertEquals(AlertLevel.WARNING, Thresholds.glucose(54))
        assertEquals(AlertLevel.NOMINAL, Thresholds.glucose(70))
        assertEquals(AlertLevel.NOMINAL, Thresholds.glucose(180))
        assertEquals(AlertLevel.WARNING, Thresholds.glucose(250))
        assertEquals(AlertLevel.CRITICAL, Thresholds.glucose(251))
    }

    @Test
    fun readinessAllNominalIsHundred() {
        val vitals = Vitals(hrBpm = 70, spo2Pct = 98)
        val manual = ManualInputs(
            glucoseMgDl = 95, stress = 1, radiationMsvPerDay = 0.5, exerciseMinPerDay = 130,
        )
        assertEquals(100, ReadinessScoring.score(vitals, manual))
    }

    @Test
    fun missingSpo2RedistributesWeight() {
        // Only HR (nominal, w .30) and exercise (critical, w .10) present.
        val vitals = Vitals(hrBpm = 70, spo2Pct = null)
        val manual = ManualInputs(exerciseMinPerDay = 40)
        // (.30*100 + .10*20) / .40 = 80
        assertEquals(80, ReadinessScoring.score(vitals, manual))
    }

    @Test
    fun triageOnTwoCriticals() {
        val readings = listOf(
            ParameterReading(HealthParameter.GLUCOSE, AlertLevel.CRITICAL),
            ParameterReading(HealthParameter.EXERCISE, AlertLevel.CRITICAL),
        )
        assertTrue(Triage.needsTriage(readings, Vitals(hrBpm = 80)))
    }

    @Test
    fun triageOnSpo2CriticalWithHighHr() {
        val readings = listOf(ParameterReading(HealthParameter.SPO2, AlertLevel.CRITICAL))
        assertTrue(Triage.needsTriage(readings, Vitals(hrBpm = 125, spo2Pct = 85)))
    }

    @Test
    fun noTriageOnSingleCritical() {
        val readings = listOf(ParameterReading(HealthParameter.EXERCISE, AlertLevel.CRITICAL))
        assertFalse(Triage.needsTriage(readings, Vitals(hrBpm = 80)))
    }

    @Test
    fun timeToCriticalProjectsLinearTrend() {
        val series = (0..5).map { TimedValue(timeMs = it * 60_000L, value = 100.0 + it * 2.0) }
        val minutes = Triage.minutesToCritical(series, criticalThreshold = 120.0)
        assertEquals(5.0, minutes!!, 1e-6)
    }

    @Test
    fun timeToCriticalNullWhenFlat() {
        val series = (0..5).map { TimedValue(timeMs = it * 60_000L, value = 100.0) }
        assertNull(Triage.minutesToCritical(series, criticalThreshold = 120.0))
    }
}
