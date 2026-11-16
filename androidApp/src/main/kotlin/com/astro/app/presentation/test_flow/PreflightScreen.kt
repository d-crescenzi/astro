package com.astro.app.presentation.test_flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.PickerState
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import com.astro.app.R
import com.astro.app.core.compose.WearPreview
import com.astro.app.core.compose.WearScreenPreview
import com.astro.app.presentation.components.AppEdgeButton
import com.astro.app.presentation.theme.AppColors

// Predefined manual ranges (shared with the health engine's inputs).
private const val GLUCOSE_MIN = 0
private const val GLUCOSE_STEP = 10
private const val GLUCOSE_COUNT = 61
private const val GLUCOSE_INIT = 10 // 100 mg/dL
private const val STRESS_MIN = 1
private const val STRESS_STEP = 1
private const val STRESS_COUNT = 5
private const val STRESS_INIT = 1 // 2 / 5
private const val EXERCISE_MIN = 0
private const val EXERCISE_STEP = 10
private const val EXERCISE_COUNT = 61
private const val EXERCISE_INIT = 9 // 90 min/day

private fun value(optionIndex: Int, min: Int, step: Int) = min + optionIndex * step

// Three sequential value pickers (glucose -> stress -> exercise); the last one confirms and starts the test.
@Composable
fun PreflightScreen(vm: TestFlowViewModel, onStartTest: () -> Unit) {
    val glucose = rememberPickerState(initialNumberOfOptions = GLUCOSE_COUNT, initiallySelectedIndex = GLUCOSE_INIT)
    val stress = rememberPickerState(initialNumberOfOptions = STRESS_COUNT, initiallySelectedIndex = STRESS_INIT)
    val exercise = rememberPickerState(initialNumberOfOptions = EXERCISE_COUNT, initiallySelectedIndex = EXERCISE_INIT)
    var index by remember { mutableIntStateOf(0) }

    when (index) {
        0 -> PreflightStep(
            title = stringResource(R.string.preflight_glucose),
            state = glucose,
            valueAt = { value(it, GLUCOSE_MIN, GLUCOSE_STEP) },
            last = false,
            onNext = { index = 1 },
        )
        1 -> PreflightStep(
            title = stringResource(R.string.preflight_stress),
            state = stress,
            valueAt = { value(it, STRESS_MIN, STRESS_STEP) },
            last = false,
            onNext = { index = 2 },
        )
        else -> PreflightStep(
            title = stringResource(R.string.preflight_exercise),
            state = exercise,
            valueAt = { value(it, EXERCISE_MIN, EXERCISE_STEP) },
            last = true,
            onNext = {
                vm.savePreflight(
                    glucoseMgDl = value(glucose.selectedOptionIndex, GLUCOSE_MIN, GLUCOSE_STEP),
                    stress = value(stress.selectedOptionIndex, STRESS_MIN, STRESS_STEP),
                    exerciseMinPerDay = value(exercise.selectedOptionIndex, EXERCISE_MIN, EXERCISE_STEP),
                )
                onStartTest()
            },
        )
    }
}

@Composable
private fun PreflightStep(
    title: String,
    state: PickerState,
    valueAt: (Int) -> Int,
    last: Boolean,
    onNext: () -> Unit,
) {
    val cd = stringResource(if (last) R.string.cd_confirm else R.string.cd_next)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 10.dp, bottom = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
            Picker(
                state = state,
                contentDescription = { title },
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { optionIndex ->
                val selected = optionIndex == state.selectedOptionIndex
                Text(
                    text = valueAt(optionIndex).toString(),
                    style = if (selected) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) AppColors.OnBackground else AppColors.TextSecondary,
                )
            }
        }
        AppEdgeButton(
            onClick = onNext,
            modifier = Modifier.align(Alignment.BottomCenter).semantics { contentDescription = cd },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.OnBackground,
                contentColor = AppColors.Background,
            ),
        ) {
            Icon(imageVector = if (last) Icons.Rounded.Check else Icons.Rounded.ChevronRight, contentDescription = null)
        }
    }
}

@WearPreview
@Composable
private fun PreflightGlucosePreview() {
    WearScreenPreview {
        PreflightStep(
            title = stringResource(R.string.preflight_glucose),
            state = rememberPickerState(initialNumberOfOptions = GLUCOSE_COUNT, initiallySelectedIndex = 6),
            valueAt = { value(it, GLUCOSE_MIN, GLUCOSE_STEP) },
            last = false,
            onNext = {},
        )
    }
}

@WearPreview
@Composable
private fun PreflightExercisePreview() {
    WearScreenPreview {
        PreflightStep(
            title = stringResource(R.string.preflight_exercise),
            state = rememberPickerState(initialNumberOfOptions = EXERCISE_COUNT, initiallySelectedIndex = 16),
            valueAt = { value(it, EXERCISE_MIN, EXERCISE_STEP) },
            last = true,
            onNext = {},
        )
    }
}
