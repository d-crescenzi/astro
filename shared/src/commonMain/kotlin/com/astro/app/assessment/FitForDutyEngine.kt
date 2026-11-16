package com.astro.app.assessment

import com.astro.app.advice.AdviceRules
import com.astro.app.baseline.BaselineCalculator
import com.astro.app.pvt.PreTestAnswers
import com.astro.app.pvt.PvtMetrics
import com.astro.app.pvt.PvtResult
import com.astro.app.pvt.PvtTrial
import com.astro.app.pvt.StimulusGenerator
import com.astro.app.pvt.TestStatus
import com.astro.app.scoring.CognitiveScoring
import com.astro.app.scoring.ReadinessScoring
import com.astro.app.vitals.BreathingGuide
import com.astro.app.vitals.ManualInputs
import com.astro.app.vitals.Thresholds
import com.astro.app.vitals.Triage
import com.astro.app.vitals.Vitals

// Public entry point of the shared engine. Deterministic and offline.
interface FitForDutyEngine {
    fun nextStimulusDelayMs(): Long
    fun evaluate(trials: List<PvtTrial>): PvtResult
    fun testStatus(trials: List<PvtTrial>): TestStatus
    fun assessCognitive(result: PvtResult, answers: PreTestAnswers, history: List<PvtResult>): CognitiveAssessment
    fun assessHealth(vitals: Vitals, manual: ManualInputs): HealthAssessment
}

// Seeded for reproducible stimulus timing within a session.
class DefaultFitForDutyEngine(seed: Long) : FitForDutyEngine {

    private val stimulus = StimulusGenerator(seed)

    override fun nextStimulusDelayMs(): Long = stimulus.nextDelayMs()

    override fun evaluate(trials: List<PvtTrial>): PvtResult = PvtMetrics.evaluate(trials)

    // False starts are taps in the empty wait, not answers: only real stimuli count toward completeness.
    override fun testStatus(trials: List<PvtTrial>): TestStatus {
        val stimuli = trials.filter { !it.falseStart }
        val answered = stimuli.count { it.responseAtMs != null }
        return when {
            answered == 0 -> TestStatus.ERROR
            answered == stimuli.size -> TestStatus.SUCCESS
            else -> TestStatus.WARNING
        }
    }

    override fun assessCognitive(
        result: PvtResult,
        answers: PreTestAnswers,
        history: List<PvtResult>,
    ): CognitiveAssessment {
        val baseline = BaselineCalculator.fromHistory(history)
        val score = CognitiveScoring.score(result, baseline)
        val status = CognitiveScoring.status(score)
        val pastStatuses = history.map { CognitiveScoring.status(CognitiveScoring.score(it, baseline)) }
        return CognitiveAssessment(
            score = score,
            status = status,
            perceivedMismatch = AdviceRules.perceivedMismatch(status, answers),
            sleepWarning = AdviceRules.sleepWarning(answers),
            advice = AdviceRules.adviceFor(status, answers),
            escalateToSurgeon = AdviceRules.shouldEscalate(status, pastStatuses),
        )
    }

    override fun assessHealth(vitals: Vitals, manual: ManualInputs): HealthAssessment {
        val readings = Thresholds.classify(vitals, manual)
        val triage = Triage.needsTriage(readings, vitals)
        return HealthAssessment(
            readings = readings,
            readinessScore = ReadinessScoring.score(vitals, manual),
            overallLevel = Triage.overallLevel(readings),
            triage = triage,
            breathingGuide = if (triage) BreathingGuide() else null,
        )
    }
}
