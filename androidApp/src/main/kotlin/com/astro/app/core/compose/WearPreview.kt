package com.astro.app.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.AppScaffold
import com.astro.app.presentation.theme.AppTheme

// Round (large/small) + square watch faces, so every screen is checked on all shapes at once.
@Preview(name = "Round (large)", device = "id:wearos_large_round", showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
@Preview(name = "Round (small)", device = "id:wearos_small_round", showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
@Preview(name = "Square", device = "id:wearos_square", showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
annotation class WearPreview

// Mirrors production: app theme + the AppScaffold that ScreenScaffold (edge button, time text) hangs off.
@Composable
fun WearScreenPreview(content: @Composable () -> Unit) {
    AppTheme {
        AppScaffold(timeText = {}) {
            content()
        }
    }
}
