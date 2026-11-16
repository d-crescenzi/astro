package com.astro.app.baseline

import com.astro.app.core.Constants
import com.astro.app.pvt.PvtResult

data class Baseline(val meanSpeed: Double, val medianRtMs: Double)

// Baseline = average of the first N sessions, same device (PLAN.md 5.3).
object BaselineCalculator {

    fun fromHistory(history: List<PvtResult>): Baseline? {
        if (history.isEmpty()) return null
        val first = history.take(Constants.BASELINE_SESSIONS)
        return Baseline(
            meanSpeed = first.map { it.meanSpeed }.average(),
            medianRtMs = first.map { it.medianRtMs }.average(),
        )
    }
}
