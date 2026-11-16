package com.astro.app.data

import com.astro.app.pvt.PvtResult
import kotlinx.serialization.Serializable

// One completed session. This shape is the contract the v2 ground dashboard reads.
@Serializable
data class SessionRecord(
    val id: String,
    val timestampMs: Long,
    val mode: String,
    val pvt: PvtSnapshot,
    val sleepHours: Float,
    val mood: Int,
    val cognitiveScore: Int,
    val status: String,
    val escalated: Boolean,
    val readinessScore: Int?,
    val overallLevel: String?,
    val hrBpm: Int?,
    val spo2Pct: Int?,
    val pendingSync: Boolean,
)

@Serializable
data class PvtSnapshot(
    val medianRtMs: Double,
    val meanSpeed: Double,
    val lapses: Int,
    val falseStarts: Int,
    val validTrials: Int,
) {
    fun toResult() = PvtResult(medianRtMs, meanSpeed, lapses, falseStarts, validTrials)

    companion object {
        fun from(result: PvtResult) = PvtSnapshot(
            result.medianRtMs, result.meanSpeed, result.lapses, result.falseStarts, result.validTrials,
        )
    }
}

// Manual inputs persisted between sessions (parameters not readable from the wrist).
@Serializable
data class ManualEntry(
    val glucoseMgDl: Int? = null,
    val stress: Int? = null,
    val radiationMsvPerDay: Double? = null,
    val exerciseMinPerDay: Int? = null,
)
