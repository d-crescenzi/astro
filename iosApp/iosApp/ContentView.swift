import SwiftUI
import Shared

enum Route: Hashable {
    case preflight, test, result
}

struct ContentView: View {
    @EnvironmentObject private var store: SessionStore
    @EnvironmentObject private var vitals: VitalsMonitor
    @EnvironmentObject private var testVM: TestFlowViewModel
    @State private var path: [Route] = []

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(
                onEnterPreflight: { testVM.begin(.demo); path = [.preflight] },
                onEnterTest: { testVM.begin(.demo); path = [.test] }
            )
            .navigationDestination(for: Route.self, destination: destination)
        }
        .tint(AppColors.onBackground)
        .onAppear { vitals.start() }
    }

    @ViewBuilder
    private func destination(_ route: Route) -> some View {
        switch route {
        case .preflight:
            PreflightView(vm: testVM, onStartTest: { path = [.test] })
        case .test:
            PvtView(vm: testVM, onFinished: { path = [.result] })
        case .result:
            ResultView(vm: testVM, onDone: { path = [] })
        }
    }
}
