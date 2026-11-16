import Foundation
import Shared

// Runs the vitals snapshot + persisted manual inputs through the shared engine.
// Used only to record a session's physiological readiness alongside the test result.
enum HealthEvaluation {
    static func assessment(engine: FitForDutyEngine,
                           snapshot: VitalsSnapshot,
                           manual: ManualEntry,
                           lastSession: SessionRecord?) -> HealthAssessment {
        let inputs = ManualInputs(
            glucoseMgDl: manual.glucoseMgDl.kotlinInt,
            stress: manual.stress.kotlinInt,
            radiationMsvPerDay: manual.radiationMsvPerDay.kotlinDouble,
            exerciseMinPerDay: manual.exerciseMinPerDay.kotlinInt,
            sleepHours: lastSession.map { KotlinFloat(float: $0.sleepHours) },
            mood: (lastSession?.mood).kotlinInt
        )
        return engine.assessHealth(vitals: snapshot.toVitals(), manual: inputs)
    }
}
