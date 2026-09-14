package com.example.a10minutesworkout

import com.example.a10minutesworkout.model.*
import org.junit.Assert.*
import org.junit.Test

class SessionEngineTest {
    private val plan = TrainingPlan("test", "Test", 0, "", "", listOf(
        TrainingStep("Prépare", 2, StepKind.PREPARE, ""), TrainingStep("Exercice", 4, StepKind.WORK, ""),
        TrainingStep("Repos", 2, StepKind.REST, ""), TrainingStep("Dernier", 4, StepKind.WORK, "")))
    @Test fun delayedTickCarriesAcrossBoundariesWithoutDrift() {
        val engine = SessionEngine(plan); engine.advance(7500)
        assertEquals(2, engine.state.index); assertEquals(500L, engine.state.remainingMillis)
        assertEquals(7500L, engine.state.elapsedMillis); assertEquals(4000L, engine.state.activeMillis)
        assertEquals(4500L, engine.remainingTotalMillis)
    }
    @Test fun pausedTimeDoesNotCount() {
        val engine = SessionEngine(plan); engine.advance(3000); engine.pause(true); engine.advance(60000)
        assertEquals(3000L, engine.state.elapsedMillis)
        engine.pause(false); engine.advance(1000); assertEquals(4000L, engine.state.elapsedMillis)
    }
    @Test fun naturalCompletionCapsTimeAtPlanDuration() {
        val engine = SessionEngine(plan); engine.advance(999999)
        assertTrue(engine.finished); assertEquals(12000L, engine.state.elapsedMillis)
        assertEquals(8000L, engine.state.activeMillis); assertEquals(0L, engine.remainingTotalMillis)
    }
    @Test fun skippingLastExerciseFinishesAndCountsOnlyActualEffort() {
        val engine = SessionEngine(plan); engine.advance(9000); engine.skip()
        assertTrue(engine.finished); assertEquals(1, engine.state.skippedWork)
        assertEquals(9000L, engine.state.elapsedMillis); assertEquals(5000L, engine.state.activeMillis)
        engine.skip(); assertEquals(1, engine.state.skippedWork)
    }
    @Test fun skippingRestDoesNotMarkExerciseSkipped() {
        val engine = SessionEngine(plan); engine.advance(6000); engine.skip()
        assertEquals(0, engine.state.skippedWork); assertEquals(3, engine.state.index)
    }
    @Test fun restoredProgressRetainsAccounting() {
        val first = SessionEngine(plan); first.advance(3500)
        val restored = SessionEngine(plan, first.state.copy(paused = true)); restored.advance(9999)
        assertEquals(3500L, restored.state.elapsedMillis)
        restored.pause(false); restored.advance(8500); assertTrue(restored.finished)
        assertEquals(8000L, restored.state.activeMillis)
    }
}
