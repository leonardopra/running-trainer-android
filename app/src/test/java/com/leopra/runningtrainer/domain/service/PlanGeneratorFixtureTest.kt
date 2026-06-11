package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

class PlanGeneratorFixtureTest {
    private val generator = PlanGenerator(idProvider = FixedIdProvider())
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun matchesFiveKBeginnerFixture() {
        val fixture = loadFixture("plan_generation_5k_beginner_age_35.json")
        val result = generator.generatePlan(fixture.request.toDomain())

        val weekOneRuns = result.plan.weeks.first().workouts.filter { it.type != WorkoutType.rest }
        val taperWeeks = result.plan.weeks.filter { it.isTaperWeek }.map { it.weekNumber }

        assertEquals(fixture.expected.totalWeeks, result.plan.totalWeeks)
        assertEquals(fixture.expected.recoveryIntervalWeeks, result.metadata.recoveryIntervalWeeks)
        assertEquals(fixture.expected.progressionRate, result.metadata.progressionRate, 0.0)
        assertEquals(fixture.expected.weeklyTargetKm, result.plan.weeks.map { it.targetWeeklyKm })
        assertEquals(fixture.expected.weekThemes, result.plan.weeks.map { it.weekTheme })
        assertEquals(fixture.expected.week1RunDays, weekOneRuns.map { it.dayOfWeek })
        assertEquals(fixture.expected.week1RunTypes, weekOneRuns.map { it.type.name })
        assertEquals(fixture.expected.week1RunTitles, weekOneRuns.map { it.title })
        assertEquals(fixture.expected.taperWeeks, taperWeeks)
        assertTrue(result.plan.weeks.all { it.workouts.size == 7 })
    }

    @Test
    fun matchesMarathonAdvancedFixture() {
        val fixture = loadFixture("plan_generation_marathon_advanced_age_52.json")
        val result = generator.generatePlan(fixture.request.toDomain())

        val weekOneRuns = result.plan.weeks.first().workouts.filter { it.type != WorkoutType.rest }
        val taperWeeks = result.plan.weeks.filter { it.isTaperWeek }.map { it.weekNumber }

        assertEquals(fixture.expected.totalWeeks, result.plan.totalWeeks)
        assertEquals(fixture.expected.recoveryIntervalWeeks, result.metadata.recoveryIntervalWeeks)
        assertEquals(fixture.expected.progressionRate, result.metadata.progressionRate, 0.0)
        assertEquals(fixture.expected.weeklyTargetKm, result.plan.weeks.map { it.targetWeeklyKm })
        assertEquals(fixture.expected.weekThemes, result.plan.weeks.map { it.weekTheme })
        assertEquals(fixture.expected.week1RunDays, weekOneRuns.map { it.dayOfWeek })
        assertEquals(fixture.expected.week1RunTypes, weekOneRuns.map { it.type.name })
        assertEquals(fixture.expected.week1RunTitles, weekOneRuns.map { it.title })
        assertEquals(fixture.expected.taperWeeks, taperWeeks)
        assertTrue(result.plan.weeks.all { it.workouts.size == 7 })
    }

    private fun loadFixture(name: String): PlanFixture {
        val candidatePaths = listOf(
            Paths.get("..", "product-spec", "fixtures", name),
            Paths.get("..", "..", "product-spec", "fixtures", name),
            Paths.get(System.getProperty("user.dir"), "..", "product-spec", "fixtures", name),
            Paths.get(System.getProperty("user.dir"), "..", "..", "product-spec", "fixtures", name)
        )
        val path = candidatePaths.firstOrNull { Files.exists(it) }
            ?: throw IllegalStateException("Unable to locate fixture $name from ${System.getProperty("user.dir")}")
        return json.decodeFromString(String(Files.readAllBytes(path), StandardCharsets.UTF_8))
    }
}

private class FixedIdProvider : () -> String {
    private var index = 0

    override fun invoke(): String {
        index += 1
        return "fixture-id-$index"
    }
}

@Serializable
private data class PlanFixture(
    val name: String,
    val request: FixtureRequest,
    val expected: FixtureExpected
)

@Serializable
private data class FixtureRequest(
    val goalType: String,
    val fitnessLevel: String,
    val trainingDaysPerWeek: Int,
    val age: Int? = null,
    val startDate: String? = null
) {
    fun toDomain(): PlanGenerationRequest = PlanGenerationRequest(
        goalType = GoalType.valueOf(goalType),
        fitnessLevel = FitnessLevel.valueOf(fitnessLevel),
        trainingDaysPerWeek = trainingDaysPerWeek,
        age = age,
        startDate = startDate?.let { LocalDate.parse(it.substring(0, 10)) }
    )
}

@Serializable
private data class FixtureExpected(
    val totalWeeks: Int,
    val recoveryIntervalWeeks: Int,
    val progressionRate: Double,
    val taperApplied: Boolean,
    val weeklyTargetKm: List<Double>,
    val weekThemes: List<String>,
    val week1RunDays: List<Int>,
    val week1RunTypes: List<String>,
    val week1RunTitles: List<String>,
    val taperWeeks: List<Int>
)
