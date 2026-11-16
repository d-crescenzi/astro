package com.astro.app

import com.astro.app.assessment.DefaultFitForDutyEngine
import com.astro.app.demo.DemoData
import com.astro.app.pvt.Status
import com.astro.app.vitals.AlertLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoScenarioTest {

    private val engine = DefaultFitForDutyEngine(seed = 1)

    @Test
    fun restedProfileIsGreenAndReady() {
        val p = DemoData.rested
        val cognitive = engine.assessCognitive(p.current, p.answers, p.history)
        assertEquals(Status.GREEN, cognitive.status)
        assertFalse(cognitive.perceivedMismatch)
        assertFalse(cognitive.escalateToSurgeon)

        val health = engine.assessHealth(p.vitals, p.manual)
        assertEquals(100, health.readinessScore)
        assertEquals(AlertLevel.NOMINAL, health.overallLevel)
        assertFalse(health.triage)
    }

    @Test
    fun tiredProfileIsRedWithMismatchAndEscalation() {
        val p = DemoData.tired
        val cognitive = engine.assessCognitive(p.current, p.answers, p.history)
        assertEquals(Status.RED, cognitive.status)
        assertTrue(cognitive.perceivedMismatch)
        assertTrue(cognitive.escalateToSurgeon)
        assertTrue(cognitive.sleepWarning)

        val health = engine.assessHealth(p.vitals, p.manual)
        assertEquals(AlertLevel.CRITICAL, health.overallLevel)
        assertFalse(health.triage)
    }
}
