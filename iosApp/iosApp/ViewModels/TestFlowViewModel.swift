import Foundation
import Shared

struct TestOutcome {
    let cognitive: CognitiveAssessment
    let result: PvtResult
}

// One view model for the whole flow: optional pre-flight -> PVT -> result.
@MainActor
final class TestFlowViewModel: ObservableObject {
    enum State {
        case idle, loading
        case success(TestOutcome)
        case failure(String)
    }

    @Published private(set) var state: State = .idle
    private(set) var mode: PvtMode = .demo

    private var engine: FitForDutyEngine = Engine.make()
    private let store: SessionStore
    private let vitals: VitalsMonitor

    init(store: SessionStore, vitals: VitalsMonitor) {
        self.store = store
        self.vitals = vitals
    }

    func begin(_ mode: PvtMode) {
        self.mode = mode
        engine = Engine.make()
        state = .idle
    }

    // Persists the pre-flight parameters so the health assessment uses them for this test.
    func savePreflight(glucoseMgDl: Int, stress: Int, exerciseMinPerDay: Int) {
        var entry = store.manual
        entry.glucoseMgDl = glucoseMgDl
        entry.stress = stress
        entry.exerciseMinPerDay = exerciseMinPerDay
        store.saveManual(entry)
    }

    func nextStimulusDelayMs() -> Int64 { engine.nextStimulusDelayMs() }

    func finish(_ trials: [PvtTrial]) {
        state = .loading
        assessAndSave(trials)
    }

    private func assessAndSave(_ trials: [PvtTrial]) {
        let past = store.sessions
        let result = engine.evaluate(trials: trials)
        let cognitive = engine.assessCognitive(
            result: result,
            answers: PreTestAnswers(sleepHours: AppConstants.sleepDefaultHours, mood: Int32(AppConstants.moodDefault)),
            history: past.map { $0.pvt.toResult() }
        )
        let snapshot = vitals.snapshot
        let health = HealthEvaluation.assessment(engine: engine, snapshot: snapshot,
                                                 manual: store.manual, lastSession: past.last)
        let isRed = ScoreStatus(cognitive.status) == .red
        let record = SessionRecord(
            id: UUID().uuidString,
            timestampMs: Int64(Date().timeIntervalSince1970 * 1000),
            mode: mode.name,
            pvt: PvtSnapshot.from(result),
            sleepHours: AppConstants.sleepDefaultHours,
            mood: AppConstants.moodDefault,
            cognitiveScore: Int(cognitive.score),
            status: cognitive.status.name,
            escalated: cognitive.escalateToSurgeon,
            readinessScore: Int(health.readinessScore),
            overallLevel: health.overallLevel.name,
            hrBpm: snapshot.hrBpm,
            spo2Pct: snapshot.spo2Pct,
            // Outbox: red results and escalations wait for the next link to Earth.
            pendingSync: isRed || cognitive.escalateToSurgeon
        )
        store.append(record)
        state = .success(TestOutcome(cognitive: cognitive, result: result))
    }
}
