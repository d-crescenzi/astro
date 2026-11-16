package com.astro.app.core

// Single source of truth for engine thresholds and weights.
// Values come from PLAN.md (PVT) and parameters.md (vitals).
object Constants {

    // --- PVT trial classification (ms) ---
    const val LAPSE_THRESHOLD_MS = 355L
    const val FALSE_START_MIN_RT_MS = 100L
    const val TIMEOUT_MS = 3_000L
    const val STIMULUS_MIN_DELAY_MS = 1_000L
    const val STIMULUS_MAX_DELAY_MS = 2_500L

    // --- Cognitive score ---
    const val BASELINE_SESSIONS = 3
    const val LAPSE_PENALTY = 5
    const val FALSE_START_PENALTY = 3
    const val STATUS_GREEN_MIN = 80
    const val STATUS_YELLOW_MIN = 60

    // --- Advice / escalation ---
    const val SLEEP_WARNING_HOURS = 6f
    const val MOOD_FEELS_GOOD_MIN = 4
    const val CONSECUTIVE_RED_FOR_ESCALATION = 2

    // --- Readiness weights (parameters.md, Demo) ---
    const val WEIGHT_HR = 0.30
    const val WEIGHT_SPO2 = 0.25
    const val WEIGHT_EXERCISE = 0.10
    const val WEIGHT_GLUCOSE = 0.15
    const val WEIGHT_STRESS = 0.10
    const val WEIGHT_RADIATION = 0.10

    // --- AlertLevel -> sub-score for the Readiness weighting (Demo) ---
    const val SCORE_NOMINAL = 100
    const val SCORE_WARNING = 60
    const val SCORE_CRITICAL = 20

    // --- Triage (parameters.md) ---
    const val TRIAGE_MIN_CRITICALS = 2
    const val HR_TRIAGE_BPM = 120

    // --- Guided breathing countermeasure ---
    const val BREATHS_PER_MIN = 6
    const val INHALE_SEC = 4
    const val EXHALE_SEC = 6

    // --- Time-to-Critical projection window (ms) ---
    const val TTC_WINDOW_MS = 5L * 60L * 1_000L

    const val SCORE_MIN = 0
    const val SCORE_MAX = 100
}
