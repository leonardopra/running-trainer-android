package com.leopra.runningtrainer.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.ui.MainUiState
import com.leopra.runningtrainer.ui.OnboardingFormState
import com.leopra.runningtrainer.ui.theme.RunningTrainerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val noPadding = PaddingValues(0.dp)

    // ── GoalSelectionScreen ───────────────────────────────────────────────────

    @Test
    fun goalSelectionScreen_showsAllGoalOptions() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                GoalSelectionScreen(innerPadding = noPadding, onGoalSelected = {})
            }
        }

        composeTestRule.onNodeWithText("5K").assertIsDisplayed()
        composeTestRule.onNodeWithText("10K").assertIsDisplayed()
        composeTestRule.onNodeWithText("Half Marathon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marathon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trail Run").assertIsDisplayed()
        composeTestRule.onNodeWithText("General Fitness").assertIsDisplayed()
    }

    @Test
    fun goalSelectionScreen_tapGoal_invokesCallback() {
        var selected: GoalType? = null
        composeTestRule.setContent {
            RunningTrainerTheme {
                GoalSelectionScreen(innerPadding = noPadding, onGoalSelected = { selected = it })
            }
        }

        composeTestRule.onNodeWithText("Marathon").performClick()
        assertEquals(GoalType.marathon, selected)
    }

    // ── FitnessSelectionScreen ────────────────────────────────────────────────

    @Test
    fun fitnessSelectionScreen_showsAllLevels() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                FitnessSelectionScreen(innerPadding = noPadding, onFitnessSelected = {})
            }
        }

        composeTestRule.onNodeWithText("Beginner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Intermediate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced").assertIsDisplayed()
    }

    @Test
    fun fitnessSelectionScreen_tapLevel_invokesCallback() {
        var selected: FitnessLevel? = null
        composeTestRule.setContent {
            RunningTrainerTheme {
                FitnessSelectionScreen(innerPadding = noPadding, onFitnessSelected = { selected = it })
            }
        }

        composeTestRule.onNodeWithText("Intermediate").performClick()
        assertEquals(FitnessLevel.intermediate, selected)
    }

    // ── TrainingDaysScreen ────────────────────────────────────────────────────

    @Test
    fun trainingDaysScreen_showsDayChips() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                TrainingDaysScreen(
                    innerPadding = noPadding,
                    selectedDays = 3,
                    onDaysChanged = {},
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("3 days").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 days").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 days").assertIsDisplayed()
        composeTestRule.onNodeWithText("6 days").assertIsDisplayed()
    }

    @Test
    fun trainingDaysScreen_tapDays_invokesCallback() {
        var days = 3
        composeTestRule.setContent {
            RunningTrainerTheme {
                TrainingDaysScreen(
                    innerPadding = noPadding,
                    selectedDays = days,
                    onDaysChanged = { days = it },
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("5 days").performClick()
        assertEquals(5, days)
    }

    // ── ProfileScreen ─────────────────────────────────────────────────────────

    @Test
    fun profileScreen_showsFormFields() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                ProfileScreen(
                    innerPadding = noPadding,
                    uiState = MainUiState(
                        onboarding = OnboardingFormState(
                            goalType = GoalType.marathon,
                            fitnessLevel = FitnessLevel.intermediate
                        )
                    ),
                    onProfileChanged = { _, _, _, _ -> },
                    onGeneratePlan = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Runner profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate Plan").assertIsDisplayed()
    }

    // ── GeneratingPlanScreen ──────────────────────────────────────────────────

    @Test
    fun generatingPlanScreen_showsBuildingMessage() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                GeneratingPlanScreen(innerPadding = noPadding)
            }
        }

        composeTestRule.onNodeWithText("Building your plan\u2026").assertIsDisplayed()
    }

    // ── RaceConfigScreen ──────────────────────────────────────────────────────

    @Test
    fun raceConfigScreen_showsDurationChips() {
        composeTestRule.setContent {
            RunningTrainerTheme {
                RaceConfigScreen(
                    innerPadding = noPadding,
                    uiState = MainUiState(
                        onboarding = OnboardingFormState(goalType = GoalType.marathon)
                    ),
                    onConfigChanged = { _, _ -> },
                    onContinue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Race setup").assertIsDisplayed()
        composeTestRule.onNodeWithText("Or choose a duration").assertIsDisplayed()
    }
}
