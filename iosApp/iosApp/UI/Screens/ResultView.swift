import SwiftUI

// Reassuring success or an at-risk warning, then it shrinks and leaves on its own.
struct ResultView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onDone: () -> Void
    @State private var closing = false

    var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()
            content
        }
        .navigationBarBackButtonHidden(true)
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case let .success(outcome):
            let atRisk = ScoreStatus(outcome.cognitive.status) == .red || outcome.cognitive.escalateToSurgeon
            outcomeView(atRisk: atRisk, text: atRisk ? L.resultAtRisk : L.resultOk)
        case let .failure(message):
            outcomeView(atRisk: true, text: message)
        default:
            ProgressView().tint(AppColors.onBackground)
        }
    }

    private func outcomeView(atRisk: Bool, text: String) -> some View {
        VStack(spacing: 10) {
            Image(systemName: atRisk ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                .font(.system(size: 52))
                .foregroundStyle(atRisk ? AppColors.error : AppColors.onBackground)
            Text(text)
                .font(.headline)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppColors.onBackground)
        }
        .padding(.horizontal, 12)
        .scaleEffect(closing ? 0.8 : 1)
        .onAppear {
            if atRisk { Haptics.failure() } else { Haptics.success() }
            Task {
                try? await Task.sleep(nanoseconds: 1_900_000_000)
                withAnimation(.easeIn(duration: 0.22)) { closing = true }
                try? await Task.sleep(nanoseconds: 230_000_000)
                onDone()
            }
        }
    }
}
