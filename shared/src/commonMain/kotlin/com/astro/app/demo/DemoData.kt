package com.astro.app.demo

import com.astro.app.pvt.PreTestAnswers
import com.astro.app.pvt.PvtResult
import com.astro.app.vitals.ManualInputs
import com.astro.app.vitals.Vitals

// Bundles a full scenario the demo can load without running a live test.
data class DemoProfile(
    val name: String,
    val history: List<PvtResult>,
    val current: PvtResult,
    val answers: PreTestAnswers,
    val vitals: Vitals,
    val manual: ManualInputs,
)

// Two preloaded profiles (PLAN.md 7).
object DemoData {

    // Astronaut A — rested: stable fast speed, everything nominal -> green.
    val rested = DemoProfile(
        name = "Astronauta A — Riposato",
        history = listOf(
            session(medianRt = 250.0, speed = 4.00),
            session(medianRt = 252.0, speed = 3.97),
            session(medianRt = 255.0, speed = 3.92),
        ),
        current = session(medianRt = 248.0, speed = 4.03),
        answers = PreTestAnswers(sleepHours = 7f, mood = 5),
        vitals = Vitals(hrBpm = 70, spo2Pct = 98),
        manual = ManualInputs(
            glucoseMgDl = 95, stress = 1, radiationMsvPerDay = 0.5,
            exerciseMinPerDay = 130, sleepHours = 7f, mood = 5,
        ),
    )

    // Astronaut B — tired: worsening curve, two reds in a row, mood still 4.
    val tired = DemoProfile(
        name = "Astronauta B — Stanco",
        history = listOf(
            session(medianRt = 250.0, speed = 4.00),
            session(medianRt = 252.0, speed = 3.97),
            session(medianRt = 255.0, speed = 3.92),
            session(medianRt = 300.0, speed = 3.10, lapses = 3),
            session(medianRt = 345.0, speed = 2.80, lapses = 5, falseStarts = 1),
        ),
        current = session(medianRt = 360.0, speed = 2.60, lapses = 6, falseStarts = 1),
        answers = PreTestAnswers(sleepHours = 5f, mood = 4),
        vitals = Vitals(hrBpm = 115, spo2Pct = 93),
        manual = ManualInputs(
            glucoseMgDl = 160, stress = 4, radiationMsvPerDay = 2.0,
            exerciseMinPerDay = 40, sleepHours = 5f, mood = 4,
        ),
    )

    private fun session(
        medianRt: Double,
        speed: Double,
        lapses: Int = 0,
        falseStarts: Int = 0,
        validTrials: Int = 45,
    ): PvtResult = PvtResult(medianRt, speed, lapses, falseStarts, validTrials)
}
