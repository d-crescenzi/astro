import SwiftUI

// Clean black-and-white palette, one red for danger and one blue for the test stimulus.
// Defined locally so the watch UI owns its look (no shared design dependency).
extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: 1)
    }
}

enum AppColors {
    static let background = Color.black
    static let onBackground = Color.white
    static let textSecondary = Color(hex: 0x84848C)
    static let primary = Color.white
    static let tertiary = Color(hex: 0x0088FF) // test stimulus only
    static let error = Color(hex: 0xFF3B30)
    static let trackDim = Color(hex: 0x3A3A3C)
}

enum AppSizes {
    static let ringStroke: CGFloat = 8
    static let stimulus: CGFloat = 80
    // Open ring: a gap centred at the top, active arc running clockwise around it.
    static let ringGapDegrees: Double = 40
}

extension ScoreStatus {
    var color: Color {
        switch self {
        case .green: return AppColors.onBackground
        case .yellow: return AppColors.onBackground
        case .red: return AppColors.error
        }
    }
}
