package com.astro.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val APPEAR_MS = 280
private val APPEAR_RISE = 8.dp
private const val BOUNCE_FROM = 0.8f
private const val BOUNCE_DELAY_MS = 140L

// Subtle one-shot entrance: fades in while rising a few pixels. Runs once per composition.
@Composable
fun Modifier.appearOnce(durationMs: Int = APPEAR_MS, rise: Dp = APPEAR_RISE): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        label = "appear",
    )
    val risePx = with(LocalDensity.current) { rise.toPx() }
    return graphicsLayer {
        alpha = appear
        translationY = (1f - appear) * risePx
    }
}

// A small one-shot hop: springs up to full size just after landing, drawing the eye to a CTA.
@Composable
fun Modifier.bounceInOnce(delayMs: Long = BOUNCE_DELAY_MS, from: Float = BOUNCE_FROM): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs)
        shown = true
    }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else from,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bounceIn",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
