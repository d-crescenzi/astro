import Foundation
import Shared

// One completed session. Shape matches the androidApp SessionRecord contract.
struct SessionRecord: Codable, Identifiable {
    let id: String
    let timestampMs: Int64
    let mode: String
    let pvt: PvtSnapshot
    let sleepHours: Float
    let mood: Int
    let cognitiveScore: Int
    let status: String
    let escalated: Bool
    let readinessScore: Int?
    let overallLevel: String?
    let hrBpm: Int?
    let spo2Pct: Int?
    let pendingSync: Bool

    var date: Date { Date(timeIntervalSince1970: Double(timestampMs) / 1000.0) }
    var scoreStatus: ScoreStatus {
        switch status {
        case "GREEN": return .green
        case "RED": return .red
        default: return .yellow
        }
    }
}

struct PvtSnapshot: Codable {
    let medianRtMs: Double
    let meanSpeed: Double
    let lapses: Int
    let falseStarts: Int
    let validTrials: Int

    func toResult() -> PvtResult {
        PvtResult(medianRtMs: medianRtMs, meanSpeed: meanSpeed,
                  lapses: Int32(lapses), falseStarts: Int32(falseStarts), validTrials: Int32(validTrials))
    }

    static func from(_ r: PvtResult) -> PvtSnapshot {
        PvtSnapshot(medianRtMs: r.medianRtMs, meanSpeed: r.meanSpeed,
                    lapses: Int(r.lapses), falseStarts: Int(r.falseStarts), validTrials: Int(r.validTrials))
    }
}

// Manual inputs persisted between sessions (not readable from the wrist).
struct ManualEntry: Codable {
    var glucoseMgDl: Int?
    var stress: Int?
    var radiationMsvPerDay: Double?
    var exerciseMinPerDay: Int?

    init(glucoseMgDl: Int? = nil, stress: Int? = nil,
         radiationMsvPerDay: Double? = nil, exerciseMinPerDay: Int? = nil) {
        self.glucoseMgDl = glucoseMgDl
        self.stress = stress
        self.radiationMsvPerDay = radiationMsvPerDay
        self.exerciseMinPerDay = exerciseMinPerDay
    }
}
