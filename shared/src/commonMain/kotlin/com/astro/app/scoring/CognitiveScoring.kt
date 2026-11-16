package com.astro.app.scoring

import com.astro.app.baseline.Baseline
import com.astro.app.core.Constants
import com.astro.app.pvt.PvtResult
import com.astro.app.pvt.Status
import kotlin.math.roundToInt

// Cognitive score and colour (PLAN.md 5.4).
object CognitiveScoring {

    // 100 * (speed / baselineSpeed) - 5*lapse - 3*falseStart, clamped 0..100.
    fun score(result: PvtResult, baseline: Baseline?): Int {
        val ratio = if (baseline != null && baseline.meanSpeed > 0.0) {
            result.meanSpeed / baseline.meanSpeed
        } else {
            1.0 // no baseline yet: compare against self, penalties still apply
        }
        val raw = Constants.SCORE_MAX * ratio -
            Constants.LAPSE_PENALTY * result.lapses -
            Constants.FALSE_START_PENALTY * result.falseStarts
        return raw.roundToInt().coerceIn(Constants.SCORE_MIN, Constants.SCORE_MAX)
    }

    fun status(score: Int): Status = when {
        score >= Constants.STATUS_GREEN_MIN -> Status.GREEN
        score >= Constants.STATUS_YELLOW_MIN -> Status.YELLOW
        else -> Status.RED
    }
}
