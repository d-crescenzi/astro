import Foundation
import Shared

enum HrSource { case sensor, simulated, simulatedOffBody }

struct TimedPoint { let timeMs: Int64; let value: Double }

struct VitalsSnapshot {
    var hrBpm: Int?
    var spo2Pct: Int = AppConstants.spo2Nominal
    var hrSource: HrSource = .simulated
    var crashActive: Bool = false
    var hrSeries: [TimedPoint] = []
    var spo2Series: [TimedPoint] = []

    func toVitals() -> Vitals {
        Vitals(hrBpm: hrBpm.kotlinInt, spo2Pct: KotlinInt(int: Int32(spo2Pct)))
    }
}

// App-wide vitals state. The simulator exposes no heart-rate stream, so HR is
// simulated exactly as the Android fallback does; a real HealthKit source can
// replace startSimulated() behind the same @Published snapshot.
@MainActor
final class VitalsMonitor: ObservableObject {
    @Published private(set) var snapshot = VitalsSnapshot()

    private var hrTask: Task<Void, Never>?
    private var crashTask: Task<Void, Never>?

    private func nowMs() -> Int64 { Int64(DispatchTime.now().uptimeNanoseconds / 1_000_000) }

    func start() {
        guard hrTask == nil else { return }
        hrTask = Task { [weak self] in
            while !Task.isCancelled {
                guard let self else { return }
                let base = AppConstants.hrSimulatedBase + self.crashOffset()
                let jitter = Int.random(in: -AppConstants.hrSimulatedJitter...AppConstants.hrSimulatedJitter)
                self.pushHr(base + jitter, source: .simulated)
                try? await Task.sleep(nanoseconds: UInt64(AppConstants.hrSimulatedPeriodSec * 1_000_000_000))
            }
        }
    }

    func stop() {
        hrTask?.cancel(); hrTask = nil
        crashTask?.cancel(); crashTask = nil
    }

    // Demo control: SpO2 drops step by step (and simulated HR climbs) to trigger triage live.
    func setCrash(_ active: Bool) {
        crashTask?.cancel(); crashTask = nil
        snapshot.crashActive = active
        guard active else { pushSpo2(AppConstants.spo2Nominal); return }
        crashTask = Task { [weak self] in
            while !Task.isCancelled {
                guard let self, self.snapshot.spo2Pct > AppConstants.spo2CrashFloor else { return }
                try? await Task.sleep(nanoseconds: UInt64(AppConstants.spo2CrashStepSec * 1_000_000_000))
                self.pushSpo2(self.snapshot.spo2Pct - 1)
            }
        }
    }

    // Under simulated HR, the crash also raises HR so the combined triage rule can fire.
    private func crashOffset() -> Int {
        let drop = AppConstants.spo2Nominal - snapshot.spo2Pct
        return min(drop * AppConstants.hrCrashStepBpm, AppConstants.hrCrashCeiling - AppConstants.hrSimulatedBase)
    }

    private func pushHr(_ bpm: Int, source: HrSource) {
        let now = nowMs()
        snapshot.hrBpm = bpm
        snapshot.hrSource = source
        snapshot.hrSeries = trim(snapshot.hrSeries + [TimedPoint(timeMs: now, value: Double(bpm))], now: now)
    }

    private func pushSpo2(_ pct: Int) {
        let now = nowMs()
        snapshot.spo2Pct = pct
        snapshot.spo2Series = trim(snapshot.spo2Series + [TimedPoint(timeMs: now, value: Double(pct))], now: now)
    }

    private func trim(_ series: [TimedPoint], now: Int64) -> [TimedPoint] {
        let windowMs = Int64(AppConstants.seriesWindowSec * 1000)
        return series.filter { $0.timeMs >= now - windowMs }
    }
}
