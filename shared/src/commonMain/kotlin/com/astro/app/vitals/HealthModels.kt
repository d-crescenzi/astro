package com.astro.app.vitals

// Nominale / Attenzione / Critico (parameters.md).
enum class AlertLevel { NOMINAL, WARNING, CRITICAL }

enum class Apparatus { CARDIOVASCULAR, RESPIRATORY, METABOLIC, RADIATION, MUSCULOSKELETAL, PSYCHOLOGICAL }

// Each parameter carries the apparatus it maps to, for the alert label.
enum class HealthParameter(val apparatus: Apparatus) {
    HR(Apparatus.CARDIOVASCULAR),
    SPO2(Apparatus.RESPIRATORY),
    EXERCISE(Apparatus.MUSCULOSKELETAL),
    GLUCOSE(Apparatus.METABOLIC),
    STRESS(Apparatus.PSYCHOLOGICAL),
    RADIATION(Apparatus.RADIATION),
    SLEEP(Apparatus.PSYCHOLOGICAL),
    MOOD(Apparatus.PSYCHOLOGICAL),
}

// SpO2 is nullable: some Apple Watch models have no sensor.
data class Vitals(val hrBpm: Int? = null, val spo2Pct: Int? = null)

data class ManualInputs(
    val glucoseMgDl: Int? = null,
    val stress: Int? = null,
    val radiationMsvPerDay: Double? = null,
    val exerciseMinPerDay: Int? = null,
    val sleepHours: Float? = null,
    val mood: Int? = null,
)

data class ParameterReading(val parameter: HealthParameter, val level: AlertLevel)
