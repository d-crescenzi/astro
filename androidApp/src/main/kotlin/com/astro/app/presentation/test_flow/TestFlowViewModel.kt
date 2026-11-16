package com.astro.app.presentation.test_flow

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astro.app.R
import com.astro.app.assessment.CognitiveAssessment
import com.astro.app.assessment.FitForDutyEngine
import com.astro.app.core.AppConstants
import com.astro.app.core.LOG
import com.astro.app.core.states.ResultState
import com.astro.app.data.HealthEvaluation
import com.astro.app.data.PvtSnapshot
import com.astro.app.data.SessionRecord
import com.astro.app.data.SessionStore
import com.astro.app.data.sensors.VitalsMonitor
import com.astro.app.pvt.PreTestAnswers
import com.astro.app.pvt.PvtMode
import com.astro.app.pvt.PvtResult
import com.astro.app.pvt.PvtTrial
import com.astro.app.pvt.Status
import com.astro.app.pvt.TestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class TestOutcome(
    val cognitive: CognitiveAssessment,
    val result: PvtResult,
    val testStatus: TestStatus,
)

// One ViewModel for the whole flow: optional pre-flight -> PVT -> result.
class TestFlowViewModel(
    private val store: SessionStore,
    private val vitals: VitalsMonitor,
    private val engineFactory: () -> FitForDutyEngine,
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<ResultState<TestOutcome>>(ResultState.Idle)
    val state: StateFlow<ResultState<TestOutcome>> = _state.asStateFlow()

    var mode: PvtMode = PvtMode.DEMO
        private set
    private var engine: FitForDutyEngine = engineFactory()

    fun begin(mode: PvtMode) {
        this.mode = mode
        engine = engineFactory()
        _state.value = ResultState.Idle
    }

    // Persists the pre-flight parameters so the health assessment uses them for this test.
    fun savePreflight(glucoseMgDl: Int, stress: Int, exerciseMinPerDay: Int) {
        viewModelScope.launch {
            val current = store.manual.first()
            store.saveManual(
                current.copy(glucoseMgDl = glucoseMgDl, stress = stress, exerciseMinPerDay = exerciseMinPerDay),
            )
        }
    }

    fun nextStimulusDelayMs(): Long = engine.nextStimulusDelayMs()

    fun finish(trials: List<PvtTrial>) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            runCatching { assessAndSave(trials) }
                .onSuccess { _state.value = ResultState.Success(it) }
                .onFailure {
                    LOG.e("Session evaluation failed", it)
                    _state.value = ResultState.Failure(context.getString(R.string.session_save_failed))
                }
        }
    }

    private suspend fun assessAndSave(trials: List<PvtTrial>): TestOutcome {
        val past = store.sessions.first()
        val result = engine.evaluate(trials)
        val cognitive = engine.assessCognitive(
            result = result,
            answers = PreTestAnswers(AppConstants.SLEEP_DEFAULT_HOURS, AppConstants.MOOD_DEFAULT),
            history = past.map { it.pvt.toResult() },
        )
        val snapshot = vitals.state.value
        val health = HealthEvaluation.evaluate(engine, snapshot, store.manual.first(), past.lastOrNull()).assessment
        store.append(
            SessionRecord(
                id = UUID.randomUUID().toString(),
                timestampMs = System.currentTimeMillis(),
                mode = mode.name,
                pvt = PvtSnapshot.from(result),
                sleepHours = AppConstants.SLEEP_DEFAULT_HOURS,
                mood = AppConstants.MOOD_DEFAULT,
                cognitiveScore = cognitive.score,
                status = cognitive.status.name,
                escalated = cognitive.escalateToSurgeon,
                readinessScore = health.readinessScore,
                overallLevel = health.overallLevel.name,
                hrBpm = snapshot.hrBpm,
                spo2Pct = snapshot.spo2Pct,
                // Outbox: red results and escalations wait for the next link to Earth.
                pendingSync = cognitive.status == Status.RED || cognitive.escalateToSurgeon,
            ),
        )
        return TestOutcome(cognitive, result, engine.testStatus(trials))
    }
}
