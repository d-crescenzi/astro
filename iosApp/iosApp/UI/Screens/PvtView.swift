import SwiftUI

struct PvtView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onFinished: () -> Void
    @StateObject private var runner: PvtRunner
    @State private var shown = false
    @State private var pulse = false

    init(vm: TestFlowViewModel, onFinished: @escaping () -> Void) {
        self.vm = vm
        self.onFinished = onFinished
        _runner = StateObject(wrappedValue: PvtRunner(durationSec: Int(vm.mode.durationSec),
                                                      nextDelay: { vm.nextStimulusDelayMs() }))
    }

    var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()
            RingView(progress: runner.progress).padding(4)
            if runner.stimulusVisible { stimulus }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            if runner.stimulusVisible { Haptics.stimulus() }
            runner.tap()
        }
        .onChange(of: runner.stimulusVisible) { _, visible in
            if visible { Haptics.stimulus() }
        }
        .navigationBarBackButtonHidden(true)
        .task {
            let trials = await runner.run()
            vm.finish(trials)
            onFinished()
        }
    }

    // A normal blue ball that springs in and pulses to draw the eye, with a tap glyph.
    private var stimulus: some View {
        ZStack {
            Circle()
                .fill(AppColors.tertiary)
                .frame(width: AppSizes.stimulus, height: AppSizes.stimulus)
                .scaleEffect(pulse ? 1.08 : 0.9)
                .animation(.easeInOut(duration: 0.85).repeatForever(autoreverses: true), value: pulse)
            Image(systemName: "hand.tap.fill")
                .font(.system(size: 28))
                .foregroundStyle(AppColors.onBackground)
        }
        .scaleEffect(shown ? 1 : 0.5)
        .opacity(shown ? 1 : 0)
        .animation(.spring(response: 0.32, dampingFraction: 0.5), value: shown)
        .onAppear { shown = true; pulse = true }
        .onDisappear { shown = false; pulse = false }
    }
}
