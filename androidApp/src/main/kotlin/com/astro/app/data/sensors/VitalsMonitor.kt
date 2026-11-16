package com.astro.app.data.sensors

import com.astro.app.core.AppConstants
import com.astro.app.core.LOG
import com.astro.app.vitals.TimedValue
import com.astro.app.vitals.Vitals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class HrSource { SENSOR, SIMULATED, SIMULATED_OFF_BODY }

data class VitalsSnapshot(
    val hrBpm: Int? = null,
    val spo2Pct: Int = AppConstants.SPO2_NOMINAL,
    val hrSource: HrSource = HrSource.SENSOR,
    val crashActive: Boolean = false,
    val hrSeries: List<TimedValue> = emptyList(),
    val spo2Series: List<TimedValue> = emptyList(),
) {
    fun toVitals() = Vitals(hrBpm = hrBpm, spo2Pct = spo2Pct)
}

// App-wide vitals state: live HR (sensor, or simulated fallback) and SpO2 with demo override.
class VitalsMonitor(
    private val heartRate: HeartRateSource,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
) {
    private val _state = MutableStateFlow(VitalsSnapshot())
    val state: StateFlow<VitalsSnapshot> = _state.asStateFlow()

    private var hrJob: Job? = null
    private var simJob: Job? = null
    private var crashJob: Job? = null

    fun start(sensorPermitted: Boolean) {
        if (hrJob?.isActive == true) return
        hrJob = if (sensorPermitted) startSensor() else startSimulated(HrSource.SIMULATED)
    }

    fun stop() {
        hrJob?.cancel()
        simJob?.cancel()
        hrJob = null
        simJob = null
    }

    // Demo control: SpO2 drops step by step (and HR climbs) to trigger triage live.
    fun setCrash(active: Boolean) {
        crashJob?.cancel()
        _state.update { it.copy(crashActive = active) }
        if (!active) {
            pushSpo2(AppConstants.SPO2_NOMINAL)
            return
        }
        crashJob = scope.launch {
            while (isActive && _state.value.spo2Pct > AppConstants.SPO2_CRASH_FLOOR) {
                delay(AppConstants.SPO2_CRASH_STEP_MS)
                pushSpo2(_state.value.spo2Pct - 1)
            }
        }
    }

    // Real readings win; while the sensor cannot measure (e.g. watch off the wrist) the demo keeps running on simulation.
    private fun startSensor(): Job = scope.launch {
        heartRate.events()
            .catch { error ->
                LOG.e("HR sensor unavailable, falling back to simulation", error)
                ensureSimulated(HrSource.SIMULATED)
            }
            .collect { event ->
                when (event) {
                    is HrEvent.Reading -> {
                        simJob?.cancel()
                        simJob = null
                        pushHr(event.bpm.toInt(), HrSource.SENSOR)
                    }
                    is HrEvent.Unavailable ->
                        ensureSimulated(if (event.offBody) HrSource.SIMULATED_OFF_BODY else HrSource.SIMULATED)
                }
            }
    }

    private fun ensureSimulated(source: HrSource) {
        if (simJob?.isActive == true) return
        simJob = startSimulated(source)
    }

    private fun startSimulated(source: HrSource): Job = scope.launch {
        while (isActive) {
            val base = AppConstants.HR_SIMULATED_BASE + crashOffset()
            pushHr(base + Random.nextInt(-AppConstants.HR_SIMULATED_JITTER, AppConstants.HR_SIMULATED_JITTER + 1), source)
            delay(AppConstants.HR_SIMULATED_PERIOD_MS)
        }
    }

    // Under simulated HR, the crash also raises HR so the combined triage rule can fire.
    private fun crashOffset(): Int {
        val drop = AppConstants.SPO2_NOMINAL - _state.value.spo2Pct
        return (drop * AppConstants.HR_CRASH_STEP_BPM)
            .coerceAtMost(AppConstants.HR_CRASH_CEILING - AppConstants.HR_SIMULATED_BASE)
    }

    private fun pushHr(bpm: Int, source: HrSource) {
        val now = clock()
        _state.update {
            it.copy(hrBpm = bpm, hrSource = source, hrSeries = trim(it.hrSeries + TimedValue(now, bpm.toDouble()), now))
        }
    }

    private fun pushSpo2(pct: Int) {
        val now = clock()
        _state.update {
            it.copy(spo2Pct = pct, spo2Series = trim(it.spo2Series + TimedValue(now, pct.toDouble()), now))
        }
    }

    private fun trim(series: List<TimedValue>, now: Long) =
        series.filter { it.timeMs >= now - AppConstants.SERIES_WINDOW_MS }
}
