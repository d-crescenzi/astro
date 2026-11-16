package com.astro.app.presentation.components

import android.os.SystemClock
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton

private const val DEBOUNCE_MS = 300L
private const val PRESS_SCALE = 0.92f

// Wraps an action so it fires at most once per 300 ms and taps the wrist each time it does.
@Composable
fun debouncedHaptic(onClick: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val last = remember { mutableLongStateOf(0L) }
    return {
        val now = SystemClock.uptimeMillis()
        if (now - last.longValue >= DEBOUNCE_MS) {
            last.longValue = now
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    }
}

// Toggle variant: haptic on every change, no debounce (switches animate their own state).
@Composable
fun hapticToggle(onChange: (Boolean) -> Unit): (Boolean) -> Unit {
    val haptic = LocalHapticFeedback.current
    return { value ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onChange(value)
    }
}

// Springy shrink while pressed, driven by the component's own interaction source.
@Composable
fun Modifier.pressBounce(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESS_SCALE else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "bounce",
    )
    return scale(scale)
}

// Bounce + debounce + haptic for arbitrary tappables (selectable items, map zones).
@Composable
fun Modifier.tappable(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val action = debouncedHaptic(onClick)
    return pressBounce(source).clickable(interactionSource = source, indication = null, onClick = action)
}

// Press bounce for a component that owns its own click but exposes no interaction source
// (e.g. the preset dialog buttons): observes presses without consuming them.
@Composable
fun Modifier.pressBounceObserving(): Modifier {
    val source = remember { MutableInteractionSource() }
    return pressBounce(source).pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val press = PressInteraction.Press(down.position)
            source.tryEmit(press)
            val up = waitForUpOrCancellation()
            source.tryEmit(if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press))
        }
    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = debouncedHaptic(onClick),
        modifier = modifier.pressBounce(source),
        colors = colors,
        interactionSource = source,
        content = content,
    )
}

@Composable
fun AppFilledTonalButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = debouncedHaptic(onClick),
        modifier = modifier.pressBounce(source),
        interactionSource = source,
        content = content,
    )
}

@Composable
fun AppEdgeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    EdgeButton(
        onClick = debouncedHaptic(onClick),
        modifier = modifier.pressBounce(source),
        colors = colors,
        interactionSource = source,
        content = content,
    )
}

@Composable
fun AppIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    FilledTonalIconButton(
        onClick = debouncedHaptic(onClick),
        modifier = modifier.pressBounce(source),
        interactionSource = source,
        content = content,
    )
}
