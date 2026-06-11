package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.TrainingWeek
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudePromptBuilderTest {

    private fun makeWorkout(id: String) = Workout(
        id = id,
        type = WorkoutType.easyRun,
        dayOfWeek = 1,
        effortLevel = EffortLevel.easy,
        title = "Easy run",
        distanceKm = 8.0
    )

    private fun makeWeek(weekNumber: Int = 1) = TrainingWeek(
        weekNumber = weekNumber,
        weekTheme = "Base Building",
        targetWeeklyKm = 30.0,
        isTaperWeek = false,
        workouts = listOf(makeWorkout("w1"), makeWorkout("w2"))
    )

    @Test
    fun `buildEnrichmentPrompt contains weekNumber, goalType and fitnessLevel`() {
        val request = ClaudePromptBuilder.buildEnrichmentPrompt(
            week = makeWeek(weekNumber = 3),
            goalType = GoalType.halfMarathon,
            fitnessLevel = FitnessLevel.intermediate,
            preferences = UserPreferencesDto()
        )

        assertTrue(request.prompt.contains("Week 3"))
        assertTrue(request.prompt.contains("halfMarathon"))
        assertTrue(request.prompt.contains("intermediate"))
    }

    @Test
    fun `buildEnrichmentPrompt with age includes Max HR with correct value`() {
        val age = 40
        val request = ClaudePromptBuilder.buildEnrichmentPrompt(
            week = makeWeek(),
            goalType = GoalType.fiveK,
            fitnessLevel = FitnessLevel.beginner,
            preferences = UserPreferencesDto(age = age)
        )

        val expectedMaxHr = 220 - age
        assertTrue(request.prompt.contains("Max HR"))
        assertTrue(request.prompt.contains(expectedMaxHr.toString()))
    }

    @Test
    fun `buildEnrichmentPrompt with null age does not include Max HR`() {
        val request = ClaudePromptBuilder.buildEnrichmentPrompt(
            week = makeWeek(),
            goalType = GoalType.fiveK,
            fitnessLevel = FitnessLevel.beginner,
            preferences = UserPreferencesDto(age = null)
        )

        assertFalse(request.prompt.contains("Max HR"))
    }

    @Test
    fun `buildPostWorkoutPrompt contains workout type, distance, RPE and feeling`() {
        val workout = Workout(
            id = "w1",
            type = WorkoutType.tempoRun,
            dayOfWeek = 3,
            effortLevel = EffortLevel.hard,
            title = "Tempo",
            distanceKm = 10.0,
            actualDistanceKm = 9.8,
            actualDurationMinutes = 48,
            rpe = 7,
            feeling = WorkoutFeeling.good,
            isCompleted = true
        )

        val request = ClaudePromptBuilder.buildPostWorkoutPrompt(workout, age = 30)

        assertTrue(request.prompt.contains("tempoRun"))
        assertTrue(request.prompt.contains("9.80 km"))
        assertTrue(request.prompt.contains("7/10"))
        assertTrue(request.prompt.contains("good"))
    }

    @Test
    fun `buildPostWorkoutPrompt returns ClaudeRequest with maxTokens 256`() {
        val workout = Workout(
            id = "w1",
            type = WorkoutType.easyRun,
            dayOfWeek = 1,
            effortLevel = EffortLevel.easy,
            title = "Easy"
        )

        val request = ClaudePromptBuilder.buildPostWorkoutPrompt(workout, age = null)

        assertEquals(256, request.maxTokens)
    }
}
