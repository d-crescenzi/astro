package com.astro.app.assessment

import com.astro.app.advice.AdviceCode
import com.astro.app.pvt.Status
import com.astro.app.vitals.AlertLevel
import com.astro.app.vitals.BreathingGuide
import com.astro.app.vitals.ParameterReading

// Cognitive outcome of one session (PLAN.md 5.12).
data class CognitiveAssessment(
    val score: Int,
    val status: Status,
    val perceivedMismatch: Boolean,
    val sleepWarning: Boolean,
    val advice: List<AdviceCode>,
    val escalateToSurgeon: Boolean,
)

// Vitals/health outcome (parameters.md).
data class HealthAssessment(
    val readings: List<ParameterReading>,
    val readinessScore: Int,
    val overallLevel: AlertLevel,
    val triage: Boolean,
    val breathingGuide: BreathingGuide?,
)
