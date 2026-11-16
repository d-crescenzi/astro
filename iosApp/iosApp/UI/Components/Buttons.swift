import SwiftUI

// Every tappable goes through these helpers: springy press bounce, a 300 ms
// debounce, and a haptic on each accepted tap — never a bare Button action.
private let debounceInterval: TimeInterval = 0.3
private let pressScale: CGFloat = 0.92
private let bounce = Animation.spring(response: 0.3, dampingFraction: 0.45)

enum ButtonKind {
    case primary, tonal

    var fill: Color {
        switch self {
        case .primary: return AppColors.onBackground
        case .tonal: return AppColors.primary.opacity(0.22)
        }
    }
    var foreground: Color {
        switch self {
        case .primary: return AppColors.background
        case .tonal: return AppColors.onBackground
        }
    }
}

private struct FilledButtonStyle: ButtonStyle {
    var fill: Color
    var foreground: Color
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.body.weight(.semibold))
            .foregroundStyle(foreground)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(fill, in: Capsule())
            .scaleEffect(configuration.isPressed ? pressScale : 1)
            .animation(bounce, value: configuration.isPressed)
    }
}

// Debounced + haptic button with a springy press.
struct AppButton<Label: View>: View {
    var kind: ButtonKind = .primary
    let action: () -> Void
    @ViewBuilder var label: () -> Label
    @State private var lastTap = Date.distantPast

    var body: some View {
        Button(action: gated, label: label)
            .buttonStyle(FilledButtonStyle(fill: kind.fill, foreground: kind.foreground))
    }

    private func gated() {
        let now = Date()
        guard now.timeIntervalSince(lastTap) >= debounceInterval else { return }
        lastTap = now
        Haptics.tap()
        action()
    }
}

extension AppButton where Label == Text {
    init(_ title: String, kind: ButtonKind = .primary, action: @escaping () -> Void) {
        self.init(kind: kind, action: action) { Text(title) }
    }
}

// Circular icon button (pre-flight next/confirm), white fill like the Wear edge button.
struct AppIconButton: View {
    let systemImage: String
    let accessibilityLabel: String
    let action: () -> Void
    @State private var lastTap = Date.distantPast

    var body: some View {
        Button {
            let now = Date()
            guard now.timeIntervalSince(lastTap) >= debounceInterval else { return }
            lastTap = now
            Haptics.tap()
            action()
        } label: {
            Image(systemName: systemImage)
                .font(.title3.weight(.semibold))
                .foregroundStyle(AppColors.background)
                .frame(width: 44, height: 44)
                .background(AppColors.onBackground, in: Circle())
        }
        .buttonStyle(IconBounceStyle())
        .accessibilityLabel(accessibilityLabel)
    }

    private struct IconBounceStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .scaleEffect(configuration.isPressed ? pressScale : 1)
                .animation(bounce, value: configuration.isPressed)
        }
    }
}
