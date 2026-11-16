import Foundation
import Shared

// Runs one PVT session on a monotonic clock shared with the tap events, so
// reaction times are exact. Mirrors the Android runPvt coroutine loop.
@MainActor
final class PvtRunner: ObservableObject {
    @Published private(set) var stimulusVisible = false
    @Published private(set) var remainingMs: Int64

    private let durationMs: Int64
    private let nextDelay: () -> Int64
    private var endAt: Int64 = 0

    // Below this much time left, don't start another stimulus: it would overrun the clock.
    private static let minResponseWindowMs: Int64 = 800

    var progress: Double { durationMs <= 0 ? 0 : Double(durationMs - remainingMs) / Double(durationMs) }

    // Single-waiter tap channel with last-value conflation (Channel.CONFLATED).
    private var waiter: CheckedContinuation<Int64?, Never>?
    private var buffered: Int64?

    init(durationSec: Int, nextDelay: @escaping () -> Int64) {
        self.durationMs = Int64(durationSec) * 1000
        self.remainingMs = self.durationMs
        self.nextDelay = nextDelay
    }

    private func nowMs() -> Int64 { Int64(DispatchTime.now().uptimeNanoseconds / 1_000_000) }

    // Called from the view on every touch.
    func tap() {
        let t = nowMs()
        if let c = waiter { waiter = nil; c.resume(returning: t) } else { buffered = t }
    }

    private func nextTap() async -> Int64? {
        if let t = buffered { buffered = nil; return t }
        return await withTaskCancellationHandler {
            await withCheckedContinuation { (c: CheckedContinuation<Int64?, Never>) in
                self.waiter = c
            }
        } onCancel: {
            Task { @MainActor in
                if let c = self.waiter { self.waiter = nil; c.resume(returning: nil) }
            }
        }
    }

    private func tapOrTimeout(ms: Int64) async -> Int64? {
        await withTaskGroup(of: Int64?.self) { group in
            group.addTask { [ms] in
                try? await Task.sleep(nanoseconds: UInt64(max(0, ms)) * 1_000_000)
                return nil
            }
            group.addTask { await self.nextTap() }
            let first = await group.next() ?? nil
            group.cancelAll()
            return first
        }
    }

    func run() async -> [PvtTrial] {
        var trials: [PvtTrial] = []
        endAt = nowMs() + durationMs
        startClock()
        func remaining() -> Int64 { max(0, endAt - nowMs()) }

        while remaining() > 0 {
            let delay = min(nextDelay(), remaining())
            if let early = await tapOrTimeout(ms: delay) {
                // A tap while waiting is a false start.
                trials.append(PvtTrial(stimulusAtMs: early,
                                       responseAtMs: KotlinLong(longLong: early),
                                       falseStart: true))
            } else if remaining() >= Self.minResponseWindowMs {
                // Cap the response window to what's left so the session never overruns its clock.
                let window = min(Int64(AppConstants.pvtTimeoutSec * 1000), remaining())
                trials.append(await presentStimulus(maxWaitMs: window))
            }
        }
        stimulusVisible = false
        return trials
    }

    private func presentStimulus(maxWaitMs: Int64) async -> PvtTrial {
        buffered = nil
        stimulusVisible = true
        // Let the ball render before stamping the shown time.
        try? await Task.sleep(nanoseconds: 32_000_000)
        let shownAt = nowMs()
        let response = await tapOrTimeout(ms: maxWaitMs)
        stimulusVisible = false
        return PvtTrial(stimulusAtMs: shownAt,
                        responseAtMs: response.map { KotlinLong(longLong: $0) },
                        falseStart: false)
    }

    private func startClock() {
        Task { [weak self] in
            guard let self else { return }
            while self.remainingMs > 0 {
                self.remainingMs = max(0, self.endAt - self.nowMs())
                try? await Task.sleep(nanoseconds: 250_000_000)
            }
        }
    }
}
