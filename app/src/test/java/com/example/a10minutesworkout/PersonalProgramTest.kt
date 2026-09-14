package com.example.a10minutesworkout

import com.example.a10minutesworkout.data.*
import com.example.a10minutesworkout.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PersonalProgramTest {
    private val today = LocalDate.of(2026, 9, 14)
    private fun session(daysAgo: Long = 2, level: Int = 0, feedback: String = "ADAPTED", completed: Boolean = true,
        id: String = "strength_a", advances: Boolean = true) = WorkoutSession(
        timestamp = today.minusDays(daysAgo).toEpochDay(), dateString = today.minusDays(daysAgo).toString(),
        workoutName = "Test", durationInSeconds = 780, programId = id, level = level, activeSeconds = 420,
        feedback = feedback, completed = completed, advancesCycle = advances)

    @Test fun durationsRemainShortAndIncludeAllTransitions() {
        assertEquals(790, PersonalProgram.build("strength_a", 0).seconds)
        assertEquals(850, PersonalProgram.build("strength_a", 1).seconds)
        assertEquals(910, PersonalProgram.build("strength_b", 2).seconds)
        for (id in listOf("strength_a", "strength_b", "cardio", "mobility")) for (level in 0..2) {
            val plan = PersonalProgram.build(id, level)
            assertTrue(plan.seconds in 780..910)
            assertTrue(plan.steps.all { it.seconds > 0 && it.cue.isNotBlank() })
            assertEquals(StepKind.WARMUP, plan.steps[1].kind)
            assertEquals(StepKind.COOLDOWN, plan.steps.last().kind)
        }
    }
    @Test fun initialPlanUsesStrengthAndIgnoresLegacyHistory() {
        assertEquals("strength_a", PersonalProgram.recommended(listOf(session(id = "")), today).id)
    }
    @Test fun completeSessionAdvancesButPartialDoesNot() {
        assertEquals("mobility", PersonalProgram.recommended(listOf(session()), today).id)
        assertEquals("strength_a", PersonalProgram.recommended(listOf(session(completed = false)), today).id)
    }
    @Test fun gentleChoiceDoesNotConsumeNextPlan() {
        assertFalse(PersonalProgram.recommended(emptyList(), today, gentle = true).advancesCycle)
        assertEquals("strength_a", PersonalProgram.recommended(listOf(session(id = "mobility", advances = false)), today).id)
    }
    @Test fun noExtraStrengthOnSameDayOrAfterPartialStrengthYesterday() {
        assertEquals("mobility", PersonalProgram.recommended(listOf(session(daysAgo = 0)), today).id)
        assertEquals("mobility", PersonalProgram.recommended(listOf(session(daysAgo = 1, completed = false)), today).id)
        assertFalse(PersonalProgram.recommended(listOf(session(daysAgo = 1, completed = false)), today).advancesCycle)
    }
    @Test fun progressRequiresThreeEasyStrengthDaysAtSameLevel() {
        assertEquals(1, PersonalProgram.recommended(listOf(session(2, feedback = "EASY"), session(4, feedback = "EASY"), session(6, feedback = "EASY")), today).level)
        assertEquals(0, PersonalProgram.recommended(listOf(session(2, feedback = "EASY"), session(4, feedback = "ADAPTED"), session(6, feedback = "EASY")), today).level)
    }
    @Test fun difficultSessionAndLongBreakReduceIntensity() {
        assertEquals(1, PersonalProgram.recommended(listOf(session(level = 2, feedback = "HARD")), today).level)
        assertEquals(1, PersonalProgram.recommended(listOf(session(daysAgo = 8, level = 2)), today).level)
    }
    @Test fun unilateralStrengthHasEqualLeftAndRightSlots() {
        for (id in listOf("strength_a", "strength_b")) {
            val steps = PersonalProgram.build(id, 0).steps.filter { it.kind == StepKind.WORK }
            assertEquals(steps.filter { it.name.endsWith("gauche") }.sumOf { it.seconds }, steps.filter { it.name.endsWith("droite") }.sumOf { it.seconds })
        }
    }
}
