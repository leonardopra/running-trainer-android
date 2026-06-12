package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RUN-16: behavioural coverage for the rule engine — km progression, age-aware
 * recovery/progression, taper, intensity distribution, weekly structure, and
 * edge cases. Values must stay in parity with product-spec/fixtures.
 */
class PlanGeneratorTest {

    private val generator = PlanGenerator(idProvider = { "id" })

    private fun generate(
        goalType: GoalType = GoalType.tenK,
        fitnessLevel: FitnessLevel = FitnessLevel.intermediate,
        trainingDaysPerWeek: Int = 4,
        durationWeeks: Int? = 12,
        age: Int? = 30
    ): TrainingPlan = generator.generatePlan(
        PlanGenerationRequest(
            goalType = goalType,
            fitnessLevel = fitnessLevel,
            trainingDaysPerWeek = trainingDaysPerWeek,
            durationWeeks = durationWeeks,
            age = age
        )
    ).plan

    // ── Km progression ────────────────────────────────────────────────────────

    @Test
    fun `base mileage follows fitness level`() {
        assertEquals(20.0, generate(fitnessLevel = FitnessLevel.beginner).weeks[0].targetWeeklyKm, 0.01)
        assertEquals(35.0, generate(fitnessLevel = FitnessLevel.intermediate).weeks[0].targetWeeklyKm, 0.01)
        assertEquals(55.0, generate(fitnessLevel = FitnessLevel.advanced).weeks[0].targetWeeklyKm, 0.01)
    }

    @Test
    fun `under 50 build weeks grow about 9 percent`() {
        val weeks = generate(age = 30).weeks
        // Weeks 2 and 3 are plain build weeks (recovery hits week 4).
        assertEquals(weeks[0].targetWeeklyKm * 1.09, weeks[1].targetWeeklyKm, 0.05)
        assertEquals(weeks[1].targetWeeklyKm * 1.09, weeks[2].targetWeeklyKm, 0.05)
    }

    @Test
    fun `over 50 build weeks grow about 7 percent`() {
        val weeks = generate(age = 55).weeks
        // Week 2 is a build week (recovery hits week 3 on the 50+ protocol).
        assertEquals(weeks[0].targetWeeklyKm * 1.07, weeks[1].targetWeeklyKm, 0.05)
    }

    // ── Age-aware recovery ────────────────────────────────────────────────────

    @Test
    fun `metadata reflects age protocol`() {
        val under50 = generator.generatePlan(
            PlanGenerationRequest(GoalType.tenK, FitnessLevel.intermediate, 4, age = 49)
        ).metadata
        val over50 = generator.generatePlan(
            PlanGenerationRequest(GoalType.tenK, FitnessLevel.intermediate, 4, age = 50)
        ).metadata

        assertEquals(4, under50.recoveryIntervalWeeks)
        assertEquals(1.09, under50.progressionRate, 0.001)
        assertEquals(3, over50.recoveryIntervalWeeks)
        assertEquals(1.07, over50.progressionRate, 0.001)
    }

    @Test
    fun `null age uses the under-50 protocol`() {
        val metadata = generator.generatePlan(
            PlanGenerationRequest(GoalType.tenK, FitnessLevel.intermediate, 4, age = null)
        ).metadata
        assertEquals(4, metadata.recoveryIntervalWeeks)
    }

    @Test
    fun `recovery week drops volume to 80 percent of the previous week`() {
        val weeks = generate(age = 30).weeks
        // Age 30 → recovery every 4th week.
        assertEquals(weeks[2].targetWeeklyKm * 0.80, weeks[3].targetWeeklyKm, 0.1)
        assertEquals("Recovery Week", weeks[3].weekTheme)
    }

    @Test
    fun `over 50 recovers every third week with dedicated theme`() {
        val weeks = generate(age = 55).weeks
        assertEquals(weeks[1].targetWeeklyKm * 0.80, weeks[2].targetWeeklyKm, 0.1)
        assertEquals("Recovery Week (50+ protocol)", weeks[2].weekTheme)
    }

    // ── Taper ─────────────────────────────────────────────────────────────────

    @Test
    fun `race goals taper the last three weeks at 70-50-30 percent of peak`() {
        val weeks = generate(goalType = GoalType.tenK, durationWeeks = 12, age = 30).weeks
        val peak = weeks.take(9).maxOf { it.targetWeeklyKm }

        assertTrue(weeks.takeLast(3).all { it.isTaperWeek })
        assertTrue(weeks.dropLast(3).none { it.isTaperWeek })
        assertEquals(peak * 0.70, weeks[9].targetWeeklyKm, 0.1)
        assertEquals(peak * 0.50, weeks[10].targetWeeklyKm, 0.1)
        assertEquals(peak * 0.30, weeks[11].targetWeeklyKm, 0.1)
    }

    @Test
    fun `general fitness plans never taper`() {
        val result = generator.generatePlan(
            PlanGenerationRequest(GoalType.generalFitness, FitnessLevel.intermediate, 4, durationWeeks = 12)
        )
        assertFalse(result.metadata.taperApplied)
        assertTrue(result.plan.weeks.none { it.isTaperWeek })
    }

    // ── Weekly structure ──────────────────────────────────────────────────────

    @Test
    fun `every week has 7 entries and the requested number of run days`() {
        for (days in 3..6) {
            val weeks = generate(trainingDaysPerWeek = days).weeks
            weeks.forEach { week ->
                assertEquals(7, week.workouts.size)
                assertEquals(days, week.workouts.count { it.type != WorkoutType.rest })
                assertEquals(7 - days, week.workouts.count { it.type == WorkoutType.rest })
            }
        }
    }

    @Test
    fun `workout distances sum to the weekly target`() {
        generate(trainingDaysPerWeek = 5).weeks.forEach { week ->
            val sum = week.workouts.sumOf { it.distanceKm ?: 0.0 }
            assertEquals(week.targetWeeklyKm, sum, 0.5)
        }
    }

    @Test
    fun `every week contains exactly one long run`() {
        for (days in 3..6) {
            generate(trainingDaysPerWeek = days).weeks.forEach { week ->
                assertEquals(1, week.workouts.count { it.type == WorkoutType.longRun })
            }
        }
    }

    @Test
    fun `hard sessions stay a minority of weekly volume`() {
        // The distribution weights keep tempo+interval km at ~17-24% of the week
        // (80/20-style polarization) for every training-days option.
        for (days in 3..6) {
            generate(trainingDaysPerWeek = days).weeks.forEach { week ->
                val total = week.workouts.sumOf { it.distanceKm ?: 0.0 }
                val hard = week.workouts
                    .filter { it.type == WorkoutType.tempoRun || it.type == WorkoutType.intervalRun }
                    .sumOf { it.distanceKm ?: 0.0 }
                assertTrue("days=$days hard=$hard total=$total", hard <= total * 0.25)
            }
        }
    }

    @Test
    fun `rest days have no distance and very easy effort`() {
        generate().weeks.flatMap { it.workouts }
            .filter { it.type == WorkoutType.rest }
            .forEach {
                assertNull(it.distanceKm)
                assertEquals(EffortLevel.veryEasy, it.effortLevel)
            }
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `duration is clamped between 4 and 24 weeks`() {
        assertEquals(4, generate(durationWeeks = 2).totalWeeks)
        assertEquals(24, generate(durationWeeks = 40).totalWeeks)
    }

    @Test
    fun `race date closer than 4 weeks still yields a 4-week plan`() {
        val plan = generator.generatePlan(
            PlanGenerationRequest(
                goalType = GoalType.fiveK,
                fitnessLevel = FitnessLevel.beginner,
                trainingDaysPerWeek = 3,
                startDate = java.time.LocalDate.of(2026, 6, 1),
                raceDate = java.time.LocalDate.of(2026, 6, 8)
            )
        ).plan
        assertEquals(4, plan.totalWeeks)
    }

    @Test
    fun `training days are coerced into the 3-6 range`() {
        val tooMany = generate(trainingDaysPerWeek = 9)
        val tooFew = generate(trainingDaysPerWeek = 1)
        assertEquals(6, tooMany.trainingDaysPerWeek)
        assertTrue(tooMany.weeks.all { w -> w.workouts.count { it.type != WorkoutType.rest } == 6 })
        assertEquals(3, tooFew.trainingDaysPerWeek)
        assertTrue(tooFew.weeks.all { w -> w.workouts.count { it.type != WorkoutType.rest } == 3 })
    }

    @Test
    fun `minimum-length race plan still tapers and keeps volumes positive`() {
        val weeks = generate(goalType = GoalType.fiveK, durationWeeks = 4, age = 70).weeks
        assertEquals(4, weeks.size)
        // With totalWeeks=4 the taper window covers weeks 2-4.
        assertEquals(listOf(false, true, true, true), weeks.map { it.isTaperWeek })
        assertTrue(weeks.all { it.targetWeeklyKm > 0.0 })
    }

    @Test
    fun `marathon plan for an older advanced runner stays consistent end to end`() {
        val weeks = generate(
            goalType = GoalType.marathon,
            fitnessLevel = FitnessLevel.advanced,
            trainingDaysPerWeek = 6,
            durationWeeks = 24,
            age = 65
        ).weeks
        assertEquals(24, weeks.size)
        assertTrue(weeks.all { it.targetWeeklyKm > 0.0 })
        assertTrue(weeks.all { it.workouts.size == 7 })
        // Peak must exceed the base despite the gentler 50+ progression.
        assertTrue(weeks.maxOf { it.targetWeeklyKm } > 55.0)
    }
}
