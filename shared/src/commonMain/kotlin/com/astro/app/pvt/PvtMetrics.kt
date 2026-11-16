package com.astro.app.pvt

import com.astro.app.core.Constants

// Turns raw trials into session metrics (PLAN.md 5.2).
object PvtMetrics {

    fun evaluate(trials: List<PvtTrial>): PvtResult {
        var falseStarts = 0
        var lapses = 0
        val rts = mutableListOf<Long>()
        for (trial in trials) {
            val rt = trial.rtMs
            when {
                trial.falseStart || (rt != null && rt < Constants.FALSE_START_MIN_RT_MS) -> falseStarts++
                rt == null -> lapses++ // no response within the window -> lapse
                else -> {
                    rts += rt
                    if (rt > Constants.LAPSE_THRESHOLD_MS) lapses++
                }
            }
        }
        return PvtResult(
            medianRtMs = median(rts),
            meanSpeed = if (rts.isEmpty()) 0.0 else rts.map { 1000.0 / it }.average(),
            lapses = lapses,
            falseStarts = falseStarts,
            validTrials = rts.size,
        )
    }

    private fun median(values: List<Long>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[mid].toDouble()
        } else {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        }
    }
}
