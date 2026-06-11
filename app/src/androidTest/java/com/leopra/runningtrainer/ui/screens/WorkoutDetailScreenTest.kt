package com.leopra.runningtrainer.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import com.leopra.runningtrainer.ui.theme.RunningTrainerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val noPadding = PaddingValues(0.dp)

    private fun makeWorkout(
        id: String = "w1",
        type: WorkoutType = WorkoutType.easyRun,
        isCompleted: Boolean = false,
        distanceKm: Double? = 5.0,
        rpe: Int? = null,
        feeling: WorkoutFeeling? = null
    ) = Workout(
        id = id,
        type = type,
        dayOfWeek = 1,
        distanceKm = distanceKm,
        durationMinutes = null,
        effortLevel = EffortLevel.easy,
        title = "5.0km Easy Run",
        description = null,
        coachingTip = null,
        isCompleted = isCompleted,
        actualDistanceKm = null,
        actualDurationMinutes = null,
        completedAt = null,
        notes = null,
        rpe = rpe,
        feeling = feeling,
        postWorkoutCoaching = null
    )

    // ── Null workout fallback ─────────────────────────────────────────────────

    @Test
    fun workoutDetailScreen_nullWorkout_showsNotFound() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = null,
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Workout not found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_nullWorkout_backButtonInvokesCallback() {
        var backCalled = false
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = null,
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = { backCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Back").performClick()
        assert(backCalled)
    }

    // ── Regular workout ───────────────────────────────────────────────────────

    @Test
    fun workoutDetailScreen_showsWorkoutTitle() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("5.0km Easy Run").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_showsMarkDoneButton_whenNotCompleted() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(isCompleted = false),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Mark done").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_showsUpdateLogButton_whenCompleted() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(isCompleted = true),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Update log").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear log").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_showsFeelingChips() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("How did you feel?").assertIsDisplayed()
        composeTestRule.onNodeWithText("🙌 Great").assertIsDisplayed()
        composeTestRule.onNodeWithText("😊 Good").assertIsDisplayed()
        composeTestRule.onNodeWithText("😐 Ok").assertIsDisplayed()
        composeTestRule.onNodeWithText("😓 Tired").assertIsDisplayed()
        composeTestRule.onNodeWithText("🤕 Injured").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_markDone_invokesOnSave() {
        var savedId: String? = null
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(id = "test-id"),
                    onSave = { id, _, _, _, _, _ -> savedId = id },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Mark done").performClick()
        assertEquals("test-id", savedId)
    }

    @Test
    fun workoutDetailScreen_clearLog_invokesOnClear() {
        var clearedId: String? = null
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(id = "clear-id", isCompleted = true),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = { clearedId = it },
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Clear log").performClick()
        assertEquals("clear-id", clearedId)
    }

    @Test
    fun workoutDetailScreen_showsPerceivedEffortSlider() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Perceived effort").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_logFields_areDisplayed() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(),
                    onSave = { _, _, _, _, _, _ -> },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Actual distance (km)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actual duration (min)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_feelingSelection_affectsSaveCallback() {
        var savedFeeling: WorkoutFeeling? = null
        composeTestRule.setContent {
            RunningTrainerTheme {
                WorkoutDetailScreen(
                    innerPadding = noPadding,
                    workout = makeWorkout(),
                    onSave = { _, _, _, _, _, feeling -> savedFeeling = feeling },
                    onClear = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("🙌 Great").performClick()
        composeTestRule.onNodeWithText("Mark done").performClick()
        assertNotNull(savedFeeling)
        assertEquals(WorkoutFeeling.great, savedFeeling)
    }
}
