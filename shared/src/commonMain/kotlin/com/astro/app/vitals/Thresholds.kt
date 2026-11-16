package com.astro.app.vitals

import com.astro.app.vitals.AlertLevel.CRITICAL
import com.astro.app.vitals.AlertLevel.NOMINAL
import com.astro.app.vitals.AlertLevel.WARNING

// Sensor and manual thresholds from parameters.md.
object Thresholds {

    fun hr(bpm: Int): AlertLevel = when {
        bpm < 50 || bpm > 120 -> CRITICAL
        bpm in 60..100 -> NOMINAL
        else -> WARNING // 50..59 or 101..120
    }

    fun spo2(pct: Int): AlertLevel = when {
        pct < 90 -> CRITICAL
        pct < 95 -> WARNING // 90..94
        else -> NOMINAL // >= 95
    }

    fun exercise(minPerDay: Int): AlertLevel = when {
        minPerDay < 60 -> CRITICAL
        minPerDay < 120 -> WARNING // 60..119
        else -> NOMINAL // >= 120
    }

    fun glucose(mgDl: Int): AlertLevel = when {
        mgDl < 54 || mgDl > 250 -> CRITICAL
        mgDl in 70..180 -> NOMINAL
        else -> WARNING // 54..69 or 181..250
    }

    fun stress(level: Int): AlertLevel = when {
        level >= 5 -> CRITICAL
        level >= 3 -> WARNING // 3..4
        else -> NOMINAL // 1..2
    }

    fun radiation(msvPerDay: Double): AlertLevel = when {
        msvPerDay > 10.0 -> CRITICAL
        msvPerDay >= 1.0 -> WARNING // 1..10
        else -> NOMINAL // < 1
    }

    fun sleep(hours: Float): AlertLevel = when {
        hours < 5f -> CRITICAL
        hours < 7f -> WARNING // 5..6.9
        else -> NOMINAL // >= 7
    }

    fun mood(level: Int): AlertLevel = when {
        level <= 1 -> CRITICAL
        level <= 3 -> WARNING // 2..3
        else -> NOMINAL // 4..5
    }

    // Classify every provided value; nulls are skipped.
    fun classify(vitals: Vitals, manual: ManualInputs): List<ParameterReading> = buildList {
        vitals.hrBpm?.let { add(ParameterReading(HealthParameter.HR, hr(it))) }
        vitals.spo2Pct?.let { add(ParameterReading(HealthParameter.SPO2, spo2(it))) }
        manual.exerciseMinPerDay?.let { add(ParameterReading(HealthParameter.EXERCISE, exercise(it))) }
        manual.glucoseMgDl?.let { add(ParameterReading(HealthParameter.GLUCOSE, glucose(it))) }
        manual.stress?.let { add(ParameterReading(HealthParameter.STRESS, stress(it))) }
        manual.radiationMsvPerDay?.let { add(ParameterReading(HealthParameter.RADIATION, radiation(it))) }
        manual.sleepHours?.let { add(ParameterReading(HealthParameter.SLEEP, sleep(it))) }
        manual.mood?.let { add(ParameterReading(HealthParameter.MOOD, mood(it))) }
    }
}
