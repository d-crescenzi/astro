package com.astro.app.scoring

import com.astro.app.core.Constants
import com.astro.app.vitals.AlertLevel
import com.astro.app.vitals.ManualInputs
import com.astro.app.vitals.Thresholds
import com.astro.app.vitals.Vitals
import kotlin.math.roundToInt

// Readiness Score (parameters.md): weighted average of the six weighted
// parameters. Missing values drop out and their weight is redistributed.
object ReadinessScoring {

    fun score(vitals: Vitals, manual: ManualInputs): Int {
        val parts = buildList {
            vitals.hrBpm?.let { add(Constants.WEIGHT_HR to levelScore(Thresholds.hr(it))) }
            vitals.spo2Pct?.let { add(Constants.WEIGHT_SPO2 to levelScore(Thresholds.spo2(it))) }
            manual.exerciseMinPerDay?.let { add(Constants.WEIGHT_EXERCISE to levelScore(Thresholds.exercise(it))) }
            manual.glucoseMgDl?.let { add(Constants.WEIGHT_GLUCOSE to levelScore(Thresholds.glucose(it))) }
            manual.stress?.let { add(Constants.WEIGHT_STRESS to levelScore(Thresholds.stress(it))) }
            manual.radiationMsvPerDay?.let { add(Constants.WEIGHT_RADIATION to levelScore(Thresholds.radiation(it))) }
        }
        val totalWeight = parts.sumOf { it.first }
        if (totalWeight <= 0.0) return Constants.SCORE_MIN
        val weighted = parts.sumOf { it.first * it.second }
        return (weighted / totalWeight).roundToInt().coerceIn(Constants.SCORE_MIN, Constants.SCORE_MAX)
    }

    fun levelScore(level: AlertLevel): Int = when (level) {
        AlertLevel.NOMINAL -> Constants.SCORE_NOMINAL
        AlertLevel.WARNING -> Constants.SCORE_WARNING
        AlertLevel.CRITICAL -> Constants.SCORE_CRITICAL
    }
}
