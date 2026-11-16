package com.astro.app.design

// Confirmed palette "Blu Apple x Arancione acceso" (docs/Palette Blu Arancione).
// Platform-agnostic ARGB values: Android wraps them in Color(...), Swift receives Int64.
object Palette {

    object Blue {
        const val B50 = 0xFFEBF4FF
        const val B100 = 0xFFD1E7FF
        const val B200 = 0xFFA3CEFF
        const val B300 = 0xFF66AEFF
        const val B400 = 0xFF3394FF
        const val B500 = 0xFF007AFF
        const val B600 = 0xFF0062CC
        const val B700 = 0xFF004C9E
        const val B800 = 0xFF003670
        const val B900 = 0xFF00224A
    }

    object Orange {
        const val O50 = 0xFFFFF3EB
        const val O100 = 0xFFFFE2CC
        const val O200 = 0xFFFFC499
        const val O300 = 0xFFFFA060
        const val O400 = 0xFFFF8533
        const val O500 = 0xFFFF6A00
        const val O600 = 0xFFD95A00
        const val O700 = 0xFFA84600
        const val O800 = 0xFF7A3300
        const val O900 = 0xFF4D2000
    }

    object Neutral {
        const val N0 = 0xFFFFFFFF
        const val N50 = 0xFFF5F5F7
        const val N100 = 0xFFE8E8ED
        const val N200 = 0xFFD2D2D7
        const val N300 = 0xFFAEAEB2
        const val N400 = 0xFF8E8E93
        const val N500 = 0xFF6E6E73
        const val N600 = 0xFF48484A
        const val N700 = 0xFF3A3A3C
        const val N800 = 0xFF1C1C1E
    }

    // Reserved for interface states (save failed, loading, done), never for health data levels.
    object Semantic {
        const val SUCCESS = 0xFF1FA851
        const val WARNING = 0xFFFFB800
        const val ERROR = 0xFFE5383B
        const val INFO = 0xFF007AFF
    }

    // Dark-mode variant of the primary, from the palette's "Dark" sample.
    const val BLUE_DARK_MODE = 0xFF0A84FF
}

// Roles the watch apps use. Change a role here and both Wear OS and watchOS follow.
object ColorRoles {
    const val BACKGROUND = 0xFF000000 // OLED watch face
    const val ON_BACKGROUND = Palette.Neutral.N0

    const val PRIMARY = Palette.BLUE_DARK_MODE // actions, PVT stimulus, breathing guide
    const val ON_PRIMARY = Palette.Neutral.N0
    const val SECONDARY = Palette.Orange.O500
    const val ON_SECONDARY = Palette.Neutral.N800 // white on orange is 2.9:1, dark is 6.6:1

    // Data levels by contrast, blue and orange only: high -> mid -> low.
    const val LEVEL_HIGH = Palette.Blue.B400
    const val LEVEL_MID = Palette.Orange.O300
    const val LEVEL_LOW = Palette.Orange.O500

    const val ACCENT_SOFT = Palette.Orange.O300 // notices: streak, sleep, sync queue
    const val ACCENT_STRONG = Palette.Orange.O500 // triage, escalation, critical

    const val TEXT_SECONDARY = Palette.Neutral.N300
    const val LABEL = Palette.Neutral.N400
    const val SURFACE = Palette.Neutral.N800
    const val PILL_CONTAINER = Palette.Neutral.N0
    const val PILL_CONTENT = Palette.Neutral.N800
}

// Same family on both watches; files live in shared/design/res/font.
object FontTokens {
    const val FONT_FAMILY = "Google Sans Flex"
}
