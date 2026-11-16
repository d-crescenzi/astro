import Foundation

// watchOS app-side constants. Engine thresholds live in the shared module.
// Mirrors androidApp core/AppConstants so both watches behave identically.
enum AppConstants {
    static let storeKeySessions = "astro.sessions"
    static let storeKeyManual = "astro.manual"
    static let storeKeyOnboarded = "astro.onboarded"

    // SpO2 (no stream on the wrist: nominal + manual override / crash demo).
    static let spo2Nominal = 98
    static let spo2CrashFloor = 84
    static let spo2CrashStepSec: Double = 4.0

    // Heart rate simulation (no sensor on the simulator; real source plugs in later).
    static let hrSimulatedBase = 72
    static let hrSimulatedJitter = 3
    static let hrSimulatedPeriodSec: Double = 1.0
    static let hrCrashStepBpm = 5
    static let hrCrashCeiling = 135

    // Vitals history kept in memory for Time-to-Critical.
    static let seriesWindowSec: Double = 5 * 60
    static let hrCriticalHigh: Double = 120
    static let spo2CriticalLow: Double = 90
    static let hrTrendMinDelta: Double = 8
    static let spo2TrendMinDelta: Double = 2

    // PVT pre-test form.
    static let sleepStepHours: Float = 0.5
    static let sleepMaxHours: Float = 12
    static let sleepDefaultHours: Float = 7
    static let moodDefault = 3

    // Guided breathing haptic pulses.
    static let inhalePulseSec: Double = 0.35

    // Manual-entry steppers.
    static let glucoseDefault = 100
    static let glucoseStep = 10
    static let glucoseMax = 600
    static let scaleDefault = 2
    static let scaleMax = 5
    static let radiationDefault = 0.5
    static let radiationStep = 0.5
    static let exerciseDefault = 90
    static let exerciseStep = 10
    static let exerciseMax = 600

    // PVT response timeout (engine Constants.TIMEOUT_MS = 3000).
    static let pvtTimeoutSec: Double = 3.0
}
