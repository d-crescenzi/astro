import Foundation

// User-facing copy (Italian), mirroring androidApp res/values/strings.xml.
enum L {
    static let appName = "Astro"

    // Home
    static func percent(_ n: Int) -> String { "\(n) %" }
    static let readiness = "🔥 Readiness"
    static let readinessIdle = "💤 Readiness"
    static let permissionsMissing = "Permessi mancanti"
    static let actionGrant = "grant"
    static let actionDoTest = "Fai il test"

    // New-values dialog
    static let dialogNewValues = "Vuoi inserire nuovi valori ?"
    static let dialogYes = "Sì"
    static let dialogNo = "No"

    // Pre-flight parameters
    static let preflightGlucose = "glicemia mg/dL"
    static let preflightStress = "stress"
    static let preflightExercise = "esercizio min/g"
    static let cdNext = "Avanti"
    static let cdConfirm = "Conferma"
    static let cdTap = "Tocca"

    // Result
    static let resultOk = "Sei pronto"
    static let resultAtRisk = "Sei a rischio"
    static let sessionSaveFailed = "Impossibile salvare la sessione."
}
