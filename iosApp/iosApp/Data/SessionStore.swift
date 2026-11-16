import Foundation
import Combine

// Offline-first local store on UserDefaults: session history (oldest first,
// with the outbox flag) and persisted manual inputs.
final class SessionStore: ObservableObject {
    @Published private(set) var sessions: [SessionRecord] = []
    @Published private(set) var manual: ManualEntry = ManualEntry()

    private let defaults: UserDefaults
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        load()
    }

    private func load() {
        if let data = defaults.data(forKey: AppConstants.storeKeySessions),
           let decoded = try? decoder.decode([SessionRecord].self, from: data) {
            sessions = decoded
        }
        if let data = defaults.data(forKey: AppConstants.storeKeyManual),
           let decoded = try? decoder.decode(ManualEntry.self, from: data) {
            manual = decoded
        }
    }

    func append(_ record: SessionRecord) {
        sessions.append(record)
        persistSessions()
    }

    func saveManual(_ entry: ManualEntry) {
        manual = entry
        if let data = try? encoder.encode(entry) {
            defaults.set(data, forKey: AppConstants.storeKeyManual)
        }
    }

    private func persistSessions() {
        if let data = try? encoder.encode(sessions) {
            defaults.set(data, forKey: AppConstants.storeKeySessions)
        }
    }

    var hasOnboarded: Bool {
        get { defaults.bool(forKey: AppConstants.storeKeyOnboarded) }
        set { defaults.set(newValue, forKey: AppConstants.storeKeyOnboarded) }
    }
}
