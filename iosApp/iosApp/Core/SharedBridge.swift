import Foundation
import Shared

// Swift-native mirror of the shared engine status enum, bridged by its Kotlin name.
// Keeps the UI off the boxed Objective-C enum singletons.
enum ScoreStatus {
    case green, yellow, red

    init(_ status: Status) {
        switch status.name {
        case "GREEN": self = .green
        case "YELLOW": self = .yellow
        default: self = .red
        }
    }
}

// Boxing helpers for the nullable Kotlin primitives the engine takes/returns.
extension Optional where Wrapped == Int {
    var kotlinInt: KotlinInt? { map { KotlinInt(int: Int32($0)) } }
}

extension Optional where Wrapped == Double {
    var kotlinDouble: KotlinDouble? { map { KotlinDouble(double: $0) } }
}

extension Optional where Wrapped == Float {
    var kotlinFloat: KotlinFloat? { map { KotlinFloat(float: $0) } }
}
