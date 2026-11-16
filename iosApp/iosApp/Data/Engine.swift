import Foundation
import Shared

// Fresh seed per session: reproducible within a run, varied across runs.
enum Engine {
    static func make() -> FitForDutyEngine {
        DefaultFitForDutyEngine(seed: Int64(Date().timeIntervalSince1970 * 1000))
    }
}
