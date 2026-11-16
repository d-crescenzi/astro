package com.astro.app.pvt

// Test length presets (PLAN.md 5.1).
enum class PvtMode(val durationSec: Int) { TRIAGE(30), DEMO(10), STANDARD(180) }

// One stimulus/response pair. Times are on a monotonic clock.
data class PvtTrial(
    val stimulusAtMs: Long,
    val responseAtMs: Long?,
    val falseStart: Boolean = false,
) {
    val rtMs: Long? get() = responseAtMs?.minus(stimulusAtMs)
}

data class PreTestAnswers(val sleepHours: Float, val mood: Int)

// Aggregate metrics of a single session.
data class PvtResult(
    val medianRtMs: Double,
    val meanSpeed: Double,
    val lapses: Int,
    val falseStarts: Int,
    val validTrials: Int,
)

enum class Status { GREEN, YELLOW, RED }

// Outcome of a test session by response completeness, independent of reaction-time scoring:
// nothing tapped, some stimuli missed, or every stimulus answered.
enum class TestStatus { ERROR, WARNING, SUCCESS }
