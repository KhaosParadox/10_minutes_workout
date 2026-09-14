package com.example.a10minutesworkout

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File

class WorkoutFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test fun startPauseSkipAndSavePartialSession() {
        compose.waitUntil(15000) { compose.onAllNodesWithText("Commencer ma séance").fetchSemanticsNodes().isNotEmpty() }
        screenshot("home.png")
        compose.onNodeWithText("Commencer ma séance").performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Passer").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Pause", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Reprendre", useUnmergedTree = true).assertExists()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Reprendre").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Reprendre", useUnmergedTree = true).performClick()
        // Reach the first exercise and inspect the compact, scrollable workout layout.
        repeat(3) { compose.onNodeWithText("Passer").performClick(); compose.waitForIdle() }
        screenshot("workout.png")
        var skipped = 0
        while (compose.onAllNodesWithText("Passer").fetchSemanticsNodes().isNotEmpty() && skipped < 40) {
            compose.onNodeWithText("Passer").performClick(); compose.waitForIdle(); skipped++
        }
        compose.onNodeWithText("Bien joué !").assertExists()
        compose.onNodeWithText("Séance partielle", substring = true).assertExists()
        compose.onNodeWithText("Enregistrer et rentrer").performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Commencer ma séance").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Calendrier", useUnmergedTree = true).performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("partielle", substring = true).fetchSemanticsNodes().isNotEmpty() }
    }
}
