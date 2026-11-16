package com.astro.app.advice

import com.astro.app.core.Constants
import com.astro.app.pvt.PreTestAnswers
import com.astro.app.pvt.Status

enum class AdviceCode { READY, TAKE_BREAK, AVOID_CRITICAL_TASKS, SLEEP_HYGIENE, PERCEIVED_MISMATCH }

// Advice and escalation rules (PLAN.md 5.6–5.9).
object AdviceRules {

    fun perceivedMismatch(status: Status, answers: PreTestAnswers): Boolean =
        answers.mood >= Constants.MOOD_FEELS_GOOD_MIN && status != Status.GREEN

    fun sleepWarning(answers: PreTestAnswers): Boolean =
        answers.sleepHours < Constants.SLEEP_WARNING_HOURS

    // Current RED plus the most recent past session RED -> escalate.
    fun shouldEscalate(current: Status, pastStatuses: List<Status>): Boolean =
        current == Status.RED && pastStatuses.lastOrNull() == Status.RED

    fun adviceFor(status: Status, answers: PreTestAnswers): List<AdviceCode> = buildList {
        when (status) {
            Status.GREEN -> add(AdviceCode.READY)
            Status.YELLOW -> add(AdviceCode.TAKE_BREAK)
            Status.RED -> add(AdviceCode.AVOID_CRITICAL_TASKS)
        }
        if (sleepWarning(answers)) add(AdviceCode.SLEEP_HYGIENE)
        if (perceivedMismatch(status, answers)) add(AdviceCode.PERCEIVED_MISMATCH)
    }
}

// Default Italian copy; the apps may localise via these codes.
object AdviceMessages {
    fun text(code: AdviceCode): String = when (code) {
        AdviceCode.READY -> "Sei pronto. Buon lavoro."
        AdviceCode.TAKE_BREAK -> "Fai una pausa di 10 minuti. Bevi acqua. Rimanda i compiti complessi se puoi."
        AdviceCode.AVOID_CRITICAL_TASKS -> "Evita compiti critici (EVA, manovre). Riposa. Rifai il test tra 1 ora."
        AdviceCode.SLEEP_HYGIENE -> "Anticipa il sonno stasera. Limita la luce intensa prima di dormire."
        AdviceCode.PERCEIVED_MISMATCH -> "Fidati del dato: chiedi a un collega di controllare il tuo lavoro."
    }
}
