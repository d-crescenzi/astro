import SwiftUI

// Single hero. Missing permission: no ring, 0 % and a grant action. Ready: the last
// test's cognitive score on a ring that fills up, and the test action.
struct HomeView: View {
    let onEnterPreflight: () -> Void
    let onEnterTest: () -> Void

    @EnvironmentObject private var store: SessionStore
    @StateObject private var gate = PermissionGate()
    @State private var askNewValues = false
    @State private var fill: Double = 0
    @State private var appeared = false

    private var lastScore: Int? { store.sessions.last?.cognitiveScore }

    var body: some View {
        let score = gate.ready ? (lastScore ?? 0) : 0
        ZStack {
            AppColors.background.ignoresSafeArea()

            if gate.ready && lastScore != nil {
                RingView(progress: fill).padding(4)
            }

            VStack(spacing: 6) {
                Text(L.percent(score))
                    .font(.system(size: score >= 100 ? 40 : 44, weight: .heavy, design: .rounded))
                    .foregroundStyle(AppColors.onBackground)
                Text(label)
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(AppColors.onBackground)
                cta
                    .frame(maxWidth: 130)
                    .padding(.top, 8)
                    .scaleEffect(appeared ? 1 : 0.84)
                    .animation(.spring(response: 0.42, dampingFraction: 0.55), value: appeared)
            }
            .opacity(appeared ? 1 : 0)
            .offset(y: appeared ? 0 : 8)
        }
        .task { await gate.refresh(); animateFill() }
        .onAppear { withAnimation(.easeOut(duration: 0.28)) { appeared = true } }
        .onChange(of: gate.ready) { animateFill() }
        .onChange(of: store.sessions.count) { animateFill() }
        .alert(L.dialogNewValues, isPresented: $askNewValues) {
            Button(L.dialogYes) { onEnterPreflight() }
            Button(L.dialogNo, role: .cancel) { onEnterTest() }
        }
    }

    private var label: String {
        if !gate.ready { return L.permissionsMissing }
        return lastScore != nil ? L.readiness : L.readinessIdle
    }

    @ViewBuilder
    private var cta: some View {
        if gate.ready {
            AppButton(L.actionDoTest) { askNewValues = true }
        } else {
            AppButton(L.actionGrant) { Task { await gate.request() } }
        }
    }

    private func animateFill() {
        let target = gate.ready ? Double(lastScore ?? 0) / 100.0 : 0
        withAnimation(.easeInOut(duration: 0.75)) { fill = target }
    }
}
