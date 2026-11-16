import SwiftUI

// Three sequential value pickers (glucose -> stress -> exercise); the last one
// confirms and starts the test. Native wheel pickers, driven by the Digital Crown.
struct PreflightView: View {
    @ObservedObject var vm: TestFlowViewModel
    let onStartTest: () -> Void

    @State private var index = 0
    @State private var glucose = AppConstants.glucoseDefault
    @State private var stress = AppConstants.scaleDefault
    @State private var exercise = AppConstants.exerciseDefault

    private let glucoseValues = Array(stride(from: 0, through: AppConstants.glucoseMax, by: AppConstants.glucoseStep))
    private let stressValues = Array(1...AppConstants.scaleMax)
    private let exerciseValues = Array(stride(from: 0, through: AppConstants.exerciseMax, by: AppConstants.exerciseStep))

    var body: some View {
        switch index {
        case 0:
            step(title: L.preflightGlucose, selection: $glucose, values: glucoseValues, last: false) { index = 1 }
        case 1:
            step(title: L.preflightStress, selection: $stress, values: stressValues, last: false) { index = 2 }
        default:
            step(title: L.preflightExercise, selection: $exercise, values: exerciseValues, last: true) {
                vm.savePreflight(glucoseMgDl: glucose, stress: stress, exerciseMinPerDay: exercise)
                onStartTest()
            }
        }
    }

    private func step(title: String, selection: Binding<Int>, values: [Int],
                      last: Bool, onNext: @escaping () -> Void) -> some View {
        VStack(spacing: 2) {
            Text(title)
                .font(.footnote.weight(.medium))
                .foregroundStyle(AppColors.onBackground)
                .padding(.top, 4)
            Picker("", selection: selection) {
                ForEach(values, id: \.self) { Text("\($0)").tag($0) }
            }
            .labelsHidden()
            .frame(maxHeight: .infinity)
        }
        .safeAreaInset(edge: .bottom) {
            AppIconButton(systemImage: last ? "checkmark" : "chevron.right",
                          accessibilityLabel: last ? L.cdConfirm : L.cdNext,
                          action: onNext)
                .padding(.bottom, 2)
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
    }
}
