package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanGeneratorSmokeTest {
    private val generator = PlanGenerator(idProvider = { "id" })

    @Test
    fun createsSevenWorkoutEntriesPerWeek() {
        val result = generator.generatePlan(
            PlanGenerationRequest(
                goalType = GoalType.tenK,
                fitnessLevel = FitnessLevel.intermediate,
                trainingDaysPerWeek = 4,
                age = 40
            )
        )

        assertTrue(result.plan.weeks.all { it.workouts.size == 7 })
    }

    @Test
    fun raceDateOverridesDefaultWeekCount() {
        val result = generator.generatePlan(
            PlanGenerationRequest(
                goalType = GoalType.halfMarathon,
                fitnessLevel = FitnessLevel.intermediate,
                trainingDaysPerWeek = 4,
                startDate = java.time.LocalDate.of(2026, 1, 1),
                raceDate = java.time.LocalDate.of(2026, 4, 1)
            )
        )

        assertEquals(12, result.plan.totalWeeks)
    }

    @Test
    fun explicitDurationOverridesDefaultWeekCount() {
        val result = generator.generatePlan(
            PlanGenerationRequest(
                goalType = GoalType.marathon,
                fitnessLevel = FitnessLevel.intermediate,
                trainingDaysPerWeek = 4,
                durationWeeks = 14
            )
        )

        assertEquals(14, result.plan.totalWeeks)
    }
}
