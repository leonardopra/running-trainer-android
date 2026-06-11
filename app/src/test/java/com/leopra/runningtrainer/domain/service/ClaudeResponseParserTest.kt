package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.TrainingWeek
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeResponseParserTest {

    private val parser = ClaudeResponseParser()

    private fun makeWorkout(id: String) = Workout(
        id = id,
        type = WorkoutType.easyRun,
        dayOfWeek = 1,
        effortLevel = EffortLevel.easy,
        title = "Easy run"
    )

    private fun makeWeek(vararg workouts: Workout) = TrainingWeek(
        weekNumber = 1,
        weekTheme = "Base",
        targetWeeklyKm = 20.0,
        isTaperWeek = false,
        workouts = workouts.toList()
    )

    @Test
    fun `valid JSON with known id applies description and coachingTip to correct workout`() {
        val week = makeWeek(makeWorkout("w1"))
        val json = """[{"id":"w1","description":"Long easy run","coachingTip":"Keep HR low"}]"""

        val enrichments = parser.parseEnrichments(json)
        val result = parser.applyEnrichments(week, enrichments)

        assertEquals("Long easy run", result.workouts[0].description)
        assertEquals("Keep HR low", result.workouts[0].coachingTip)
    }

    @Test
    fun `valid JSON with unknown id leaves workout unchanged`() {
        val week = makeWeek(makeWorkout("w1"))
        val json = """[{"id":"unknown","description":"desc","coachingTip":"tip"}]"""

        val enrichments = parser.parseEnrichments(json)
        val result = parser.applyEnrichments(week, enrichments)

        assertNull(result.workouts[0].description)
        assertNull(result.workouts[0].coachingTip)
    }

    @Test
    fun `response wrapped in json code fences still parses correctly`() {
        val week = makeWeek(makeWorkout("w2"))
        val json = "```json\n[{\"id\":\"w2\",\"description\":\"Tempo run\",\"coachingTip\":\"Push pace\"}]\n```"

        val enrichments = parser.parseEnrichments(json)
        val result = parser.applyEnrichments(week, enrichments)

        assertEquals("Tempo run", result.workouts[0].description)
        assertEquals("Push pace", result.workouts[0].coachingTip)
    }

    @Test
    fun `malformed JSON returns emptyMap without throwing`() {
        val result = parser.parseEnrichments("{{{not valid json at all")

        assertTrue(result.isEmpty())
    }
}
