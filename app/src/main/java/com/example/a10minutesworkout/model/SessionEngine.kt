package com.example.a10minutesworkout.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionProgress(
    val index: Int = 0,
    val remainingMillis: Long = 0,
    val elapsedMillis: Long = 0,
    val activeMillis: Long = 0,
    val skippedWork: Int = 0,
    val paused: Boolean = false
)

/** Pure elapsed-time reducer; the caller supplies monotonic deltas and owns lifecycle pauses. */
class SessionEngine(val plan: TrainingPlan, restored: SessionProgress? = null) {
    var state = restored ?: SessionProgress(remainingMillis = plan.steps.first().seconds * 1000L)
        private set
    val finished get() = state.index >= plan.steps.size
    val step get() = plan.steps.getOrNull(state.index)
    val remainingTotalMillis get() = state.remainingMillis + plan.steps.drop(state.index + 1).sumOf { it.seconds * 1000L }

    fun pause(paused: Boolean) { state = state.copy(paused = paused) }

    fun advance(deltaMillis: Long) {
        if (state.paused || finished) return
        var delta = deltaMillis.coerceAtLeast(0)
        while (delta > 0 && !finished) {
            val used = minOf(delta, state.remainingMillis)
            state = state.copy(remainingMillis = state.remainingMillis - used,
                elapsedMillis = state.elapsedMillis + used,
                activeMillis = state.activeMillis + if (step?.kind == StepKind.WORK) used else 0)
            delta -= used
            if (state.remainingMillis == 0L) next()
        }
    }

    fun skip() {
        if (finished) return
        if (step?.kind == StepKind.WORK) state = state.copy(skippedWork = state.skippedWork + 1)
        next()
    }

    private fun next() {
        val index = state.index + 1
        state = state.copy(index = index, remainingMillis = plan.steps.getOrNull(index)?.seconds?.times(1000L) ?: 0)
    }
}
