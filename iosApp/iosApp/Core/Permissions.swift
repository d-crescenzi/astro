import Foundation
import UserNotifications

// Permission gate for the home. Heart rate is simulated on the wrist, so the one
// real runtime permission is notifications (as requested on Android too). Once the
// user has answered, the home switches from the grant CTA to the test CTA.
@MainActor
final class PermissionGate: ObservableObject {
    @Published private(set) var ready = false

    private let center = UNUserNotificationCenter.current()

    func refresh() async {
        let status = await center.notificationSettings().authorizationStatus
        ready = status != .notDetermined
    }

    func request() async {
        _ = try? await center.requestAuthorization(options: [.alert, .sound])
        ready = true
    }
}
