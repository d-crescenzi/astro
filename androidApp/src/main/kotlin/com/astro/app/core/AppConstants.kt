package com.astro.app.core

// Android-side constants. Engine thresholds live in shared core/Constants.kt.
object AppConstants {

    const val DATASTORE_NAME = "astro"
    const val LOG_TAG = "Astro"

    // --- Reminders (morning / evening) ---
    const val REMINDER_CHANNEL_ID = "pvt_reminders"
    const val REMINDER_WORK_NAME = "pvt_reminder"
    const val REMINDER_NOTIFICATION_ID = 1001
    val REMINDER_HOURS = listOf(8, 20)

    // --- SpO2 (Pixel Watch exposes no stream: nominal + manual override) ---
    const val SPO2_NOMINAL = 98
    const val SPO2_CRASH_FLOOR = 84
    const val SPO2_CRASH_STEP_MS = 4_000L

    // --- Heart rate fallback when the sensor is unavailable (emulator) ---
    const val HR_SIMULATED_BASE = 72
    const val HR_SIMULATED_JITTER = 3
    const val HR_SIMULATED_PERIOD_MS = 1_000L
    const val HR_CRASH_STEP_BPM = 5
    const val HR_CRASH_CEILING = 135

    // --- Vitals history kept in memory for Time-to-Critical ---
    const val SERIES_WINDOW_MS = 5L * 60L * 1_000L
    const val HR_CRITICAL_HIGH = 120.0
    const val SPO2_CRITICAL_LOW = 90.0
    // Demo: a trend must move at least this much in the window before Time-to-Critical is projected.
    const val HR_TREND_MIN_DELTA = 8.0
    const val SPO2_TREND_MIN_DELTA = 2.0

    // --- PVT session ---
    const val SLEEP_STEP_HOURS = 0.5f
    const val SLEEP_MAX_HOURS = 12f
    const val SLEEP_DEFAULT_HOURS = 7f
    const val MOOD_DEFAULT = 3

    // --- Haptic breathing pulses ---
    const val INHALE_PULSE_MS = 350L
    const val EXHALE_PULSE_MS = 120L
    const val EXHALE_GAP_MS = 120L
}
