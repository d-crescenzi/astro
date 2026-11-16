import SwiftUI

// Determinate ring with a gap centred at the top. A native trimmed circle, not a
// hand-drawn shape: track underneath, indicator on top.
struct RingView: View {
    var progress: Double
    var indicator: Color = AppColors.onBackground
    var track: Color = AppColors.trackDim
    var stroke: CGFloat = AppSizes.ringStroke

    private var sweep: Double { (360 - AppSizes.ringGapDegrees) / 360 }

    var body: some View {
        ZStack {
            Circle()
                .trim(from: 0, to: sweep)
                .stroke(track, style: StrokeStyle(lineWidth: stroke, lineCap: .round))
            Circle()
                .trim(from: 0, to: sweep * progress.clamped(0, 1))
                .stroke(indicator, style: StrokeStyle(lineWidth: stroke, lineCap: .round))
        }
        .rotationEffect(.degrees(-90 + AppSizes.ringGapDegrees / 2))
        .padding(stroke / 2)
    }
}

extension Comparable {
    func clamped(_ lo: Self, _ hi: Self) -> Self { min(max(self, lo), hi) }
}
