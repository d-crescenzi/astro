package com.astro.app.pvt

import com.astro.app.core.Constants
import kotlin.random.Random

// Seeded so a run is reproducible for demo and tests (PLAN.md 5.1).
class StimulusGenerator(
    seed: Long,
    private val minDelayMs: Long = Constants.STIMULUS_MIN_DELAY_MS,
    private val maxDelayMs: Long = Constants.STIMULUS_MAX_DELAY_MS,
) {
    private val random = Random(seed)

    fun nextDelayMs(): Long = random.nextLong(minDelayMs, maxDelayMs + 1)
}
