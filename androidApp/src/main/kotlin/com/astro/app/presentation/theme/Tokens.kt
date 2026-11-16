package com.astro.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.astro.app.R

// Black-and-white palette: neutrals only, a single red for danger and one blue for the test stimulus.
object AppColors {
    val Background = Color(0xFF000000)
    val OnBackground = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF84848C)
    val Primary = Color(0xFFFFFFFF)
    val Tertiary = Color(0xFF0088FF)
    val Error = Color(0xFFFF3B30)
    val Warning = Color(0xFFFFCC00)
    val TrackDim = Color(0xFF3A3A3C)
}

// Dimensions Material does not provide out of the box.
object AppSizes {
    val Ring = 92.dp
    val RingStroke = 8.dp
    val Stimulus = 96.dp
    val StimulusGlow = 20.dp
}

val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

// Display face for the home readiness percentage only; everything else uses Google Sans Flex.
val RammettoOne = FontFamily(Font(R.font.rammetto_one))

// Monochrome scheme so default components (buttons, pickers, dialogs) read black-and-white;
// tertiary carries the only accent, the test stimulus.
private val AppColorScheme = ColorScheme(
    primary = Color(0xFFFFFFFF),
    primaryDim = Color(0xFFE5E5EA),
    primaryContainer = Color(0xFF2C2C2E),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF84848C),
    secondaryDim = Color(0xFF6E6E73),
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondary = Color(0xFF000000),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF0088FF),
    tertiaryDim = Color(0xFF0062CC),
    tertiaryContainer = Color(0xFF00224A),
    onTertiary = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF6E6E73),
    outlineVariant = Color(0xFF48484A),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography(defaultFontFamily = GoogleSansFlex),
        content = content,
    )
}
