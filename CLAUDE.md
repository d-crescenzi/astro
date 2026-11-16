# Astro — Wear OS + watchOS

Smartwatch-only app. **No phone targets.**

- **`androidApp`** — Wear OS app (Jetpack Compose for Wear, Material 3). `minSdk 30` (Wear OS 3+).
- **`iosApp`** — watchOS app (SwiftUI), deployment target watchOS 11.
- **`shared`** — Kotlin Multiplatform logic shared by both watches (Android + `watchosArm64` /
  `watchosSimulatorArm64`), exported as the `Shared` framework. UI is **native per
  platform** (Compose MP does not support watchOS), so nothing UI-related lives here.

---

## Conventions

These are the intended conventions as code grows — the project is currently a clean skeleton.

- **`shared` exposes only use cases.** Layering: UI → ViewModel (native) → UseCase → Service →
  SDK/Settings. No service, SDK type, or Settings crosses into the apps; everything not consumed by
  the apps is `internal`.
- **Two result wrappers in `shared`**: `RestrictedResultState<T>` and `RestrictedEmptyResultState`
  (`shared/core/states`, Success/Failure only). No `Result`, `Either`, or other wrappers. The apps
  own the 4-state UI wrappers `ResultState<T>` / `EmptyResultState` (Idle/Loading/Success/Failure —
  presentation only, one wrapped state flow per ViewModel, always starting `Idle`).
- **Manual Koin only** — no annotations, no codegen. `sharedModule(...)` + per-app modules (a
  ViewModel per screen, or one per flow whose pages share it). The Apple side talks to `shared` only
  through a single DI entry point (`PlatformModule`): one operation per use-case operation,
  `async throws` on networked ones, `StateFlow` properties for observable state.
- **Strings (Android)**: every user-facing string lives in `res/values/strings.xml`; no hardcoded
  display text in Kotlin. Enum-to-label mappings return `@StringRes` ids, resolved with
  `stringResource` in composables or `context.getString` elsewhere.
- **Empty states (Android)**: every empty state shows a Material Icons **Outlined** icon above its
  text (the `EmptyState` component). Outlined family only — no filled icons.
- **Tappables (Android)**: every tappable (buttons, edge buttons, steppers, selectable items) goes
  through the shared interaction helpers — press bounce, 300 ms debounce, and haptic on each accepted
  tap — never a bare Material `onClick`.
- **Comments are agnostic**: describe what the code does and the invariant it keeps, never its
  history or sources. No references to mockups, CSS, design specs, API-level trivia, tickets, or
  "it was like this before / I read it here". If a line only justifies a past decision, delete it.
- **Style**: comments minimal (max 3 lines, no essays); multi-word packages are `snake_case`
  (`forgot_password`, `use_cases`) in both `shared` and `androidApp`; one constants file per module
  (`shared` / `androidApp` `core/Constants.kt`, watchOS `Core/Constants.swift`). Apps log only through
  debug-gated `LOG` helpers (no raw `Log.*` / `print` / `NSLog`); `shared` logs through Kermit. No
  `Facade` in names.
- **Quality gate**: `./gradlew check` (detekt + Android Lint) must stay green.
- **Git**: Conventional Commits, messages always in English.

## Build & verify

- Android (Wear OS): `./gradlew :androidApp:assembleDebug`
- watchOS: `cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=watchOS Simulator' build`
- Full check: `./gradlew check`
