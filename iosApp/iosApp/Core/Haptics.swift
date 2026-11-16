import WatchKit

// Wrist biofeedback through the native watchOS haptic engine.
enum Haptics {
    static func tap() { WKInterfaceDevice.current().play(.click) }
    static func stimulus() { WKInterfaceDevice.current().play(.notification) }
    static func success() { WKInterfaceDevice.current().play(.success) }
    static func failure() { WKInterfaceDevice.current().play(.failure) }
}
