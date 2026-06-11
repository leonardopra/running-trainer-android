package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgressStatsCalculatorTest {
    @Test
    fun computesCompletionAndLoggedDistanceFromCompletedWorkouts() {
        val generator = PlanGenerator(
            clock = object : Clock {
                override fun now(): Instant = Instant.parse("2026-01-20T07:00:00Z")
            },
            idProvider = ProgressTestIdProvider()
        )
        val calculator = ProgressStatsCalculator(
            clock = object : Clock {
                override fun now(): Instant = Instant.parse("2026-01-20T07:00:00Z")
            }
        )

        val basePlan = generator.generatePlan(
            PlanGenerationRequest(
                goalType = GoalType.fiveK,
                fitnessLevel = FitnessLevel.beginner,
                trainingDaysPerWeek = 3,
                age = 35,
                startDate = LocalDate.of(2026, 1, 5)
            )
        ).plan

        val completedPlan = basePlan.copy(
            weeks = basePlan.weeks.mapIndexed { weekIndex, week ->
                if (weekIndex != 0) {
                    week
                } else {
                    week.copy(
                        workouts = week.workouts.mapIndexed { workoutIndex, workout ->
                            when (workoutIndex) {
                                0 -> workout.copy(
                                    isCompleted = true,
                                    actualDistanceKm = 5.0,
                                    actualDurationMinutes = 31,
                                    completedAt = Instant.parse("2026-01-05T07:00:00Z"),
                                    rpe = 4,
                                    feeling = WorkoutFeeling.good
                                )
                                2 -> workout.copy(
                                    isCompleted = true,
                                    actualDistanceKm = 9.8,
                                    actualDurationMinutes = 62,
                                    completedAt = Instant.parse("2026-01-07T07:00:00Z"),
                                    rpe = 7,
                                    feeling = WorkoutFeeling.tired
                                )
                                else -> workout
                            }
                        }
                    )
                }
            }
        )

        val stats = calculator.compute(completedPlan)

        assertEquals(24, stats.totalNonRestWorkouts)
        assertEquals(2, stats.completedWorkouts)
        assertEquals(14.8, stats.totalLoggedKm, 0.0)
        assertEquals(2, stats.rpeDataPoints.size)
        assertEquals(1, stats.feelingCounts[WorkoutFeeling.good])
        assertEquals(1, stats.feelingCounts[WorkoutFeeling.tired])
        assertTrue(stats.weeklyProgress.isNotEmpty())
    }
}

private class ProgressTestIdProvider : () -> String {
    private var index = 0

    override fun invoke(): String {
        index += 1
        return "progress-fixture-$index"
    }
}
