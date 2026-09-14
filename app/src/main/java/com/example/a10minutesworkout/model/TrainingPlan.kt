package com.example.a10minutesworkout.model

import kotlinx.serialization.Serializable

@Serializable
enum class StepKind { PREPARE, WARMUP, WORK, REST, COOLDOWN }

@Serializable
data class TrainingStep(
    val name: String,
    val seconds: Int,
    val kind: StepKind,
    val cue: String,
    val easier: String = "Ralentis et réduis l’amplitude.",
    val gif: String? = null
)

@Serializable
data class TrainingPlan(
    val id: String,
    val title: String,
    val level: Int,
    val equipment: String,
    val description: String,
    val steps: List<TrainingStep>,
    val advancesCycle: Boolean = true
) {
    val seconds: Int get() = steps.sumOf { it.seconds }
    val isStrength: Boolean get() = id.startsWith("strength")
}

fun formatDuration(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
