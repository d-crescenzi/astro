import SwiftUI

@main
struct AstroApp: App {
    @StateObject private var store: SessionStore
    @StateObject private var vitals: VitalsMonitor
    @StateObject private var testVM: TestFlowViewModel

    init() {
        let store = SessionStore()
        let vitals = VitalsMonitor()
        _store = StateObject(wrappedValue: store)
        _vitals = StateObject(wrappedValue: vitals)
        _testVM = StateObject(wrappedValue: TestFlowViewModel(store: store, vitals: vitals))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(store)
                .environmentObject(vitals)
                .environmentObject(testVM)
                .tint(AppColors.onBackground)
        }
    }
}
