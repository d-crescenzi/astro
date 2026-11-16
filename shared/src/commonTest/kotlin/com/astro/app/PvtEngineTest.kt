package com.astro.app

import com.astro.app.advice.AdviceCode
import com.astro.app.advice.AdviceRules
import com.astro.app.assessment.DefaultFitForDutyEngine
import com.astro.app.baseline.Baseline
import com.astro.app.baseline.BaselineCalculator
import com.astro.app.pvt.PreTestAnswers
import com.astro.app.pvt.PvtMetrics
import com.astro.app.pvt.PvtResult
import com.astro.app.pvt.PvtTrial
import com.astro.app.pvt.Status
import com.astro.app.pvt.StimulusGenerator
import com.astro.app.pvt.TestStatus
import com.astro.app.scoring.CognitiveScoring
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PvtEngineTest {

    private fun responded(rt: Long) = PvtTrial(stimulusAtMs = 0, responseAtMs = rt)
    private fun timeout() = PvtTrial(stimulusAtMs = 0, responseAtMs = null)

    @Test
    fun tapBeforeStimulusIsFalseStart() {
        val result = PvtMetrics.evaluate(listOf(PvtTrial(stimulusAtMs = 100, responseAtMs = 150, falseStart = true)))
        assertEquals(1, result.falseStarts)
        assertEquals(0, result.validTrials)
    }

    @Test
    fun tooFastResponseIsFalseStart() {
        val result = PvtMetrics.evaluate(listOf(responded(80)))
        assertEquals(1, result.falseStarts)
    }

    @Test
    fun slowResponseIsLapse() {
        val result = PvtMetrics.evaluate(listOf(responded(400)))
        assertEquals(1, result.lapses)
        assertEquals(1, result.validTrials)
    }

    @Test
    fun noResponseIsLapse() {
        val result = PvtMetrics.evaluate(listOf(timeout()))
        assertEquals(1, result.lapses)
        assertEquals(0, result.validTrials)
    }

    @Test
    fun medianAndMeanSpeedOnKnownData() {
        val result = PvtMetrics.evaluate(listOf(responded(200), responded(300), responded(400)))
        assertEquals(300.0, result.medianRtMs, 1e-6)
        assertEquals((5.0 + 1000.0 / 300.0 + 2.5) / 3.0, result.meanSpeed, 1e-6)
    }

    @Test
    fun scoreIsClampedToRange() {
        val fast = PvtResult(200.0, 8.0, 0, 0, 10)
        assertEquals(100, CognitiveScoring.score(fast, Baseline(4.0, 250.0)))
        val slow = PvtResult(900.0, 0.5, 10, 5, 10)
        assertEquals(0, CognitiveScoring.score(slow, Baseline(4.0, 250.0)))
    }

    @Test
    fun colourThresholds() {
        assertEquals(Status.GREEN, CognitiveScoring.status(80))
        assertEquals(Status.YELLOW, CognitiveScoring.status(79))
        assertEquals(Status.YELLOW, CognitiveScoring.status(60))
        assertEquals(Status.RED, CognitiveScoring.status(59))
    }

    @Test
    fun baselineIsAverageOfFirstThree() {
        val history = listOf(
            PvtResult(250.0, 4.0, 0, 0, 10),
            PvtResult(250.0, 3.0, 0, 0, 10),
            PvtResult(250.0, 2.0, 0, 0, 10),
            PvtResult(250.0, 1.0, 0, 0, 10),
        )
        assertEquals(3.0, BaselineCalculator.fromHistory(history)!!.meanSpeed, 1e-6)
    }

    @Test
    fun perceivedMismatchWhenFeelsGoodButNotGreen() {
        val feelsGood = PreTestAnswers(sleepHours = 7f, mood = 4)
        assertTrue(AdviceRules.perceivedMismatch(Status.RED, feelsGood))
        assertFalse(AdviceRules.perceivedMismatch(Status.GREEN, feelsGood))
    }

    @Test
    fun escalationOnTwoRedsInARow() {
        assertTrue(AdviceRules.shouldEscalate(Status.RED, listOf(Status.YELLOW, Status.RED)))
        assertFalse(AdviceRules.shouldEscalate(Status.RED, listOf(Status.RED, Status.YELLOW)))
        assertFalse(AdviceRules.shouldEscalate(Status.YELLOW, listOf(Status.RED)))
    }

    @Test
    fun adviceCollectsAllTriggers() {
        val advice = AdviceRules.adviceFor(Status.RED, PreTestAnswers(sleepHours = 5f, mood = 4))
        assertTrue(advice.contains(AdviceCode.AVOID_CRITICAL_TASKS))
        assertTrue(advice.contains(AdviceCode.SLEEP_HYGIENE))
        assertTrue(advice.contains(AdviceCode.PERCEIVED_MISMATCH))
    }

    @Test
    fun sameSeedProducesSameSequence() {
        val a = StimulusGenerator(seed = 42)
        val b = StimulusGenerator(seed = 42)
        repeat(5) { assertEquals(a.nextDelayMs(), b.nextDelayMs()) }
    }

    @Test
    fun delaysStayWithinRange() {
        val gen = StimulusGenerator(seed = 7)
        repeat(50) {
            val delay = gen.nextDelayMs()
            assertTrue(delay in 1_000L..4_000L)
        }
    }

    @Test
    fun testStatusErrorWhenNothingAnswered() {
        val engine = DefaultFitForDutyEngine(seed = 1)
        assertEquals(TestStatus.ERROR, engine.testStatus(listOf(timeout(), timeout())))
    }

    @Test
    fun testStatusWarningWhenSomeMissed() {
        val engine = DefaultFitForDutyEngine(seed = 1)
        assertEquals(TestStatus.WARNING, engine.testStatus(listOf(responded(280), timeout())))
    }

    @Test
    fun testStatusSuccessWhenAllAnswered() {
        val engine = DefaultFitForDutyEngine(seed = 1)
        assertEquals(TestStatus.SUCCESS, engine.testStatus(listOf(responded(280), responded(310))))
    }

    @Test
    fun testStatusIgnoresFalseStarts() {
        val engine = DefaultFitForDutyEngine(seed = 1)
        val falseStart = PvtTrial(stimulusAtMs = 0, responseAtMs = 50, falseStart = true)
        assertEquals(TestStatus.SUCCESS, engine.testStatus(listOf(falseStart, responded(280))))
    }
}
