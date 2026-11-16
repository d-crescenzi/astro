package com.astro.app.presentation.test_flow

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ripple
import com.astro.app.R
import com.astro.app.core.Constants
import com.astro.app.core.compose.WearPreview
import com.astro.app.core.compose.WearScreenPreview
import com.astro.app.presentation.components.Cookie9Shape
import com.astro.app.presentation.theme.AppColors
import com.astro.app.presentation.theme.AppSizes
import com.astro.app.pvt.PvtTrial
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private const val MS_PER_SEC = 1_000L
private const val CLOCK_TICK_MS = 100L
private const val RING_START_ANGLE = 290f
private const val RING_END_ANGLE = 250f
private val RING_INSET = 4.dp
private const val PULSE_MIN = 0.9f
private const val PULSE_MAX = 1.08f
private const val PULSE_MS = 850
private const val STIMULUS_ICON_DP = 36
// Below this much time left, don't start another stimulus: it would overrun the session clock.
private const val MIN_RESPONSE_WINDOW_MS = 800L

@Composable
fun PvtScreen(vm: TestFlowViewModel, onFinished: () -> Unit) {
    var stimulusVisible by remember { mutableStateOf(false) }
    val totalMs = vm.mode.durationSec * MS_PER_SEC
    var progress by remember { mutableFloatStateOf(0f) }
    val taps = remember { Channel<Long>(Channel.CONFLATED) }
    val haptic = LocalHapticFeedback.current
    KeepScreenOn()

    val endAt = remember { SystemClock.uptimeMillis() + totalMs }
    LaunchedEffect(endAt) {
        while (progress < 1f) {
            val remaining = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
            progress = ((totalMs - remaining).toFloat() / totalMs).coerceIn(0f, 1f)
            delay(CLOCK_TICK_MS)
        }
    }
    LaunchedEffect(Unit) {
        val trials = runPvt(
            durationMs = totalMs,
            nextDelay = vm::nextStimulusDelayMs,
            taps = taps,
            onStimulus = { stimulusVisible = it },
        )
        vm.finish(trials)
        onFinished()
    }

    // A tap in the empty wait is a false start; a visible stimulus is tapped on its own shape.
    val visible = rememberUpdatedState(stimulusVisible)
    val waitTapModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (!visible.value) taps.trySend(down.uptimeMillis)
        }
    }
    PvtContent(
        progress = progress,
        stimulusVisible = stimulusVisible,
        waitTapModifier = waitTapModifier,
        onStimulusTap = { at ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            taps.trySend(at)
        },
    )
}

// Progress ring around the bezel; the stimulus appears at the centre.
@Composable
private fun PvtContent(
    progress: Float,
    stimulusVisible: Boolean,
    waitTapModifier: Modifier = Modifier,
    onStimulusTap: (Long) -> Unit = {},
) {
    // Read the latest progress inside the draw lambda so the indicator follows the running clock.
    val liveProgress = rememberUpdatedState(progress)
    Box(
        modifier = Modifier.fillMaxSize().then(waitTapModifier),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { liveProgress.value },
            modifier = Modifier.fillMaxSize().padding(RING_INSET),
            startAngle = RING_START_ANGLE,
            endAngle = RING_END_ANGLE,
            strokeWidth = AppSizes.RingStroke,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = AppColors.OnBackground,
                trackColor = AppColors.TrackDim,
            ),
        )
        if (stimulusVisible) Stimulus(onTap = onStimulusTap)
    }
}

// Nine-lobed cookie that springs in and pulses to draw the eye; a tap shows a ripple clipped
// to its shape and reports the moment it went down.
@Composable
private fun Stimulus(onTap: (Long) -> Unit) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    val shape = remember { Cookie9Shape() }
    val interaction = remember { MutableInteractionSource() }

    // Entrance: springs up from nothing so it never pops in abruptly.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "appear",
    )
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = PULSE_MIN,
        targetValue = PULSE_MAX,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .size(AppSizes.Stimulus)
            .scale(appear * pulse)
            .alpha(appear)
            .clip(shape)
            .background(AppColors.Tertiary)
            .indication(interaction, ripple(color = AppColors.OnBackground))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    interaction.tryEmit(press)
                    onTap(down.uptimeMillis)
                    val up = waitForUpOrCancellation()
                    interaction.tryEmit(
                        if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.TouchApp,
            contentDescription = stringResource(R.string.cd_tap),
            tint = AppColors.OnBackground,
            modifier = Modifier.size(STIMULUS_ICON_DP.dp),
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

// Runs the session on the monotonic clock shared with pointer events, so reaction times are exact.
private suspend fun runPvt(
    durationMs: Long,
    nextDelay: () -> Long,
    taps: Channel<Long>,
    onStimulus: (Boolean) -> Unit,
): List<PvtTrial> {
    val trials = mutableListOf<PvtTrial>()
    val endAt = SystemClock.uptimeMillis() + durationMs
    fun remaining() = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
    while (remaining() > 0) {
        // A tap while waiting is a false start.
        val early = withTimeoutOrNull(minOf(nextDelay(), remaining())) { taps.receive() }
        when {
            early != null -> trials += PvtTrial(stimulusAtMs = early, responseAtMs = early, falseStart = true)
            // Only present when a fair response window still fits, and cap it to what's left:
            // this keeps the session from overrunning its clock by a full timeout at the end.
            remaining() >= MIN_RESPONSE_WINDOW_MS ->
                trials += presentStimulus(taps, onStimulus, minOf(Constants.TIMEOUT_MS, remaining()))
        }
    }
    return trials
}

// Stamps the stimulus after its frame is drawn, not when it is requested.
private suspend fun presentStimulus(taps: Channel<Long>, onStimulus: (Boolean) -> Unit, maxWaitMs: Long): PvtTrial {
    onStimulus(true)
    withFrameNanos { }
    withFrameNanos { }
    val shownAt = SystemClock.uptimeMillis()
    val response = withTimeoutOrNull(maxWaitMs) { taps.receive() }
    onStimulus(false)
    return PvtTrial(stimulusAtMs = shownAt, responseAtMs = response)
}

@WearPreview
@Composable
private fun PvtWaitingPreview() {
    WearScreenPreview { PvtContent(progress = 0.35f, stimulusVisible = false) }
}

@WearPreview
@Composable
private fun PvtStimulusPreview() {
    WearScreenPreview { PvtContent(progress = 0.6f, stimulusVisible = true) }
}
