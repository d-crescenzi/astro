package com.astro.app.presentation.test_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.FailureConfirmationDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.confirmationDialogCurvedText
import com.astro.app.R
import com.astro.app.assessment.CognitiveAssessment
import com.astro.app.core.compose.WearPreview
import com.astro.app.core.compose.WearScreenPreview
import com.astro.app.core.states.ResultState
import com.astro.app.presentation.components.DiamondShape
import com.astro.app.presentation.theme.AppColors
import com.astro.app.pvt.PvtResult
import com.astro.app.pvt.Status
import com.astro.app.pvt.TestStatus

private const val SUCCESS_CLOSE_MS = 220
private const val SUCCESS_CLOSE_SCALE = 0.8f
// A touch shorter than the native 4s default, so the result lingers but does not drag.
private const val RESULT_DURATION_MS = 3_000L
private const val WARNING_DIAMOND_DP = 80
private const val WARNING_ICON_DP = 36

@Composable
fun ResultScreen(vm: TestFlowViewModel, onDone: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    ResultContent(state = state, onDone = onDone)
}

// Native confirmation, by test completeness: a reassuring white success when every stimulus was
// answered, a yellow warning when some were missed, or a red failure when none were.
@Composable
private fun ResultContent(state: ResultState<TestOutcome>, onDone: () -> Unit) {
    when (state) {
        is ResultState.Success -> when (state.data.testStatus) {
            TestStatus.SUCCESS -> SuccessResult(text = stringResource(R.string.result_ok), onDone = onDone)
            TestStatus.WARNING -> WarningResult(text = stringResource(R.string.result_warning), onDone = onDone)
            TestStatus.ERROR -> FailureResult(text = stringResource(R.string.result_at_risk), onDone = onDone)
        }

        is ResultState.Failure -> FailureResult(text = state.message, onDone = onDone)

        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SuccessResult(text: String, onDone: () -> Unit) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    // When its time is up, shrink a touch and then leave, rather than snapping shut.
    var visible by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else SUCCESS_CLOSE_SCALE,
        animationSpec = tween(SUCCESS_CLOSE_MS),
        label = "close",
    )
    LaunchedEffect(visible) {
        if (!visible) {
            delay(SUCCESS_CLOSE_MS.toLong())
            onDone()
        }
    }
    SuccessConfirmationDialog(
        visible = visible,
        onDismissRequest = { visible = false },
        durationMillis = RESULT_DURATION_MS,
        modifier = Modifier.scale(scale),
        curvedText = { confirmationDialogCurvedText(text, style) },
        colors = ConfirmationDialogDefaults.colors(
            iconColor = AppColors.Background,
            iconContainerColor = AppColors.OnBackground,
            textColor = AppColors.OnBackground,
        ),
    )
}

@Composable
private fun FailureResult(text: String, onDone: () -> Unit) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    FailureConfirmationDialog(
        visible = true,
        onDismissRequest = onDone,
        durationMillis = RESULT_DURATION_MS,
        curvedText = { confirmationDialogCurvedText(text, style) },
        colors = ConfirmationDialogDefaults.colors(
            iconColor = AppColors.OnBackground,
            iconContainerColor = AppColors.Error,
            textColor = AppColors.OnBackground,
        ),
    ) {
        Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null)
    }
}

// Partial run: a yellow diamond holding a warning mark, over the native confirmation scaffold.
@Composable
private fun WarningResult(text: String, onDone: () -> Unit) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    ConfirmationDialog(
        visible = true,
        onDismissRequest = onDone,
        durationMillis = RESULT_DURATION_MS,
        curvedText = { confirmationDialogCurvedText(text, style) },
        // Transparent container hides the native hero shape; the diamond below is the whole mark.
        colors = ConfirmationDialogDefaults.colors(
            iconColor = AppColors.Background,
            iconContainerColor = Color.Transparent,
            textColor = AppColors.OnBackground,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(WARNING_DIAMOND_DP.dp)
                .clip(DiamondShape())
                .background(AppColors.Warning),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PriorityHigh,
                contentDescription = null,
                tint = AppColors.Background,
                modifier = Modifier.size(WARNING_ICON_DP.dp),
            )
        }
    }
}

private val PREVIEW_PVT = PvtResult(medianRtMs = 280.0, meanSpeed = 3.6, lapses = 1, falseStarts = 0, validTrials = 20)

private fun previewOutcome(testStatus: TestStatus) = TestOutcome(
    cognitive = CognitiveAssessment(
        score = if (testStatus == TestStatus.ERROR) 38 else 86,
        status = if (testStatus == TestStatus.ERROR) Status.RED else Status.GREEN,
        perceivedMismatch = false,
        sleepWarning = false,
        advice = emptyList(),
        escalateToSurgeon = false,
    ),
    result = PREVIEW_PVT,
    testStatus = testStatus,
)

@WearPreview
@Composable
private fun ResultSuccessPreview() {
    WearScreenPreview { ResultContent(state = ResultState.Success(previewOutcome(TestStatus.SUCCESS)), onDone = {}) }
}

@WearPreview
@Composable
private fun ResultWarningPreview() {
    WearScreenPreview { ResultContent(state = ResultState.Success(previewOutcome(TestStatus.WARNING)), onDone = {}) }
}

@WearPreview
@Composable
private fun ResultFailurePreview() {
    WearScreenPreview { ResultContent(state = ResultState.Success(previewOutcome(TestStatus.ERROR)), onDone = {}) }
}
