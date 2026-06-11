package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.TrainingWeek
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ClaudeResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseEnrichments(response: String): Map<String, Pair<String?, String?>> {
        val cleaned = response.trim().replace(Regex("```[a-z]*\\n?"), "").trim()
        return try {
            json.parseToJsonElement(cleaned).jsonArray.associate { element ->
                val obj = element.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: return@associate "" to (null to null)
                id to (obj["description"]?.jsonPrimitive?.content to obj["coachingTip"]?.jsonPrimitive?.content)
            }.filterKeys { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun applyEnrichments(
        week: TrainingWeek,
        enrichments: Map<String, Pair<String?, String?>>
    ): TrainingWeek {
        return week.copy(
            workouts = week.workouts.map { workout ->
                val (description, coachingTip) = enrichments[workout.id] ?: return@map workout
                workout.copy(description = description, coachingTip = coachingTip)
            }
        )
    }
}
