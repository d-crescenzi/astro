package com.astro.app.data

import com.astro.app.assessment.FitForDutyEngine
import com.astro.app.assessment.HealthAssessment
import com.astro.app.core.AppConstants
import com.astro.app.data.sensors.VitalsSnapshot
import com.astro.app.vitals.AlertLevel
import com.astro.app.vitals.HealthParameter
import com.astro.app.vitals.ManualInputs
import com.astro.app.vitals.TimedValue
import com.astro.app.vitals.Triage

data class HealthView(
    val snapshot: VitalsSnapshot,
    val manual: ManualEntry,
    val assessment: HealthAssessment,
    val minutesToCritical: Double?,
)

// Glue between live vitals, persisted manual inputs and the shared engine.
object HealthEvaluation {

    fun evaluate(
        engine: FitForDutyEngine,
        snapshot: VitalsSnapshot,
        manual: ManualEntry,
        lastSession: SessionRecord?,
    ): HealthView {
        val inputs = ManualInputs(
            glucoseMgDl = manual.glucoseMgDl,
            stress = manual.stress,
            radiationMsvPerDay = manual.radiationMsvPerDay,
            exerciseMinPerDay = manual.exerciseMinPerDay,
            sleepHours = lastSession?.sleepHours,
            mood = lastSession?.mood,
        )
        val assessment = engine.assessHealth(snapshot.toVitals(), inputs)
        return HealthView(snapshot, manual, assessment, timeToCritical(snapshot, assessment))
    }

    // 0 if HR or SpO2 is already critical, else the nearest linear projection (null = stable).
    private fun timeToCritical(snapshot: VitalsSnapshot, assessment: HealthAssessment): Double? {
        val alreadyCritical = assessment.readings.any {
            it.level == AlertLevel.CRITICAL && it.parameter in setOf(HealthParameter.HR, HealthParameter.SPO2)
        }
        if (alreadyCritical) return 0.0
        return listOfNotNull(
            snapshot.hrSeries.takeIf { it.moved(AppConstants.HR_TREND_MIN_DELTA) }
                ?.let { Triage.minutesToCritical(it, AppConstants.HR_CRITICAL_HIGH) },
            snapshot.spo2Series.takeIf { it.moved(AppConstants.SPO2_TREND_MIN_DELTA) }
                ?.let { Triage.minutesToCritical(it, AppConstants.SPO2_CRITICAL_LOW) },
        ).minOrNull()
    }

    // Sensor jitter must not produce a projection: require a real excursion in the window.
    private fun List<TimedValue>.moved(minDelta: Double): Boolean =
        isNotEmpty() && (maxOf { it.value } - minOf { it.value }) >= minDelta
}
