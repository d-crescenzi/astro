package com.astro.app.vitals

import com.astro.app.core.Constants

// Guided breathing cadence for the countermeasure (parameters.md).
data class BreathingGuide(
    val breathsPerMin: Int = Constants.BREATHS_PER_MIN,
    val inhaleSec: Int = Constants.INHALE_SEC,
    val exhaleSec: Int = Constants.EXHALE_SEC,
)

data class TimedValue(val timeMs: Long, val value: Double)

object Triage {

    fun overallLevel(readings: List<ParameterReading>): AlertLevel = when {
        readings.any { it.level == AlertLevel.CRITICAL } -> AlertLevel.CRITICAL
        readings.any { it.level == AlertLevel.WARNING } -> AlertLevel.WARNING
        else -> AlertLevel.NOMINAL
    }

    // >= 2 Critical, or SpO2 Critical together with HR > 120 (parameters.md 5).
    fun needsTriage(readings: List<ParameterReading>, vitals: Vitals): Boolean {
        val criticals = readings.count { it.level == AlertLevel.CRITICAL }
        if (criticals >= Constants.TRIAGE_MIN_CRITICALS) return true
        val spo2Critical = readings.any {
            it.parameter == HealthParameter.SPO2 && it.level == AlertLevel.CRITICAL
        }
        val hrOverLimit = (vitals.hrBpm ?: 0) > Constants.HR_TRIAGE_BPM
        return spo2Critical && hrOverLimit
    }

    // Project the last 5 minutes in a straight line to the critical threshold.
    // Returns minutes until it is reached, or null if not trending toward it.
    fun minutesToCritical(series: List<TimedValue>, criticalThreshold: Double): Double? {
        val last = series.maxByOrNull { it.timeMs } ?: return null
        val window = series
            .filter { it.timeMs >= last.timeMs - Constants.TTC_WINDOW_MS }
            .sortedBy { it.timeMs }
        val slopePerMin = linearSlopePerMs(window)
            ?.takeIf { window.size >= 2 && it != 0.0 }
            ?.times(60_000.0)
        return slopePerMin
            ?.let { (criticalThreshold - last.value) / it }
            ?.takeIf { it > 0.0 }
    }

    private fun linearSlopePerMs(points: List<TimedValue>): Double? {
        val n = points.size
        val meanX = points.sumOf { it.timeMs.toDouble() } / n
        val meanY = points.sumOf { it.value } / n
        var num = 0.0
        var den = 0.0
        for (p in points) {
            val dx = p.timeMs - meanX
            num += dx * (p.value - meanY)
            den += dx * dx
        }
        return if (den == 0.0) null else num / den
    }
}
