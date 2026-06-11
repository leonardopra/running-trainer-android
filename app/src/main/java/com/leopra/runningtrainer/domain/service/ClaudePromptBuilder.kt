package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.TrainingWeek
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object ClaudePromptBuilder {

    fun buildEnrichmentPrompt(
        week: TrainingWeek,
        goalType: GoalType,
        fitnessLevel: FitnessLevel,
        preferences: UserPreferencesDto
    ): ClaudeRequest {
        val nonRest = week.workouts.filter { it.type != WorkoutType.rest }

        val profileContext = preferences.age?.let { age ->
            val maxHr = 220 - age
            "\nRunner profile: age $age" +
                (preferences.weightKg?.let { ", ${it.toInt()}kg" } ?: "") +
                (preferences.heightCm?.let { ", ${it.toInt()}cm" } ?: "") +
                ".\nMax HR ≈ $maxHr bpm. Include age-appropriate recovery cues and HR zone guidance."
        } ?: ""

        val workoutsJson = buildJsonArray {
            nonRest.forEach { w ->
                add(buildJsonObject {
                    put("id", w.id)
                    put("type", w.type.name)
                    put("title", w.title)
                    w.distanceKm?.let { put("distanceKm", it) }
                    w.durationMinutes?.let { put("durationMinutes", it) }
                })
            }
        }

        val prompt = "Week ${week.weekNumber}: ${week.weekTheme}\n" +
            "Target: ${week.targetWeeklyKm}km\n" +
            "Goal: ${goalType.name} | Level: ${fitnessLevel.name}$profileContext\n\n" +
            "Workouts to enrich:\n$workoutsJson\n\n" +
            "Return ONLY a JSON array with this structure for each workout:\n" +
            "[{\"id\": \"...\", \"description\": \"...\", \"coachingTip\": \"...\"}]\n\n" +
            "Rules: max 60 words per description, direct/practical tone, no markdown."

        return ClaudeRequest(prompt = prompt)
    }

    fun buildPostWorkoutPrompt(
        workout: Workout,
        age: Int?
    ): ClaudeRequest {
        val distStr = workout.actualDistanceKm?.let { String.format(java.util.Locale.US, "%.2f km", it) } ?: "unknown distance"
        val durStr = workout.actualDurationMinutes?.let { "$it min" } ?: "unknown duration"
        val rpeStr = workout.rpe?.let { "$it/10" } ?: "not logged"
        val feelingStr = workout.feeling?.name ?: "not logged"
        val notesStr = workout.notes?.takeIf { it.isNotBlank() } ?: "none"
        val ageStr = if (age != null) ", age $age" else ""
        val typeLabel = workout.type.name

        val prompt = "Athlete$ageStr completed a $typeLabel " +
            "(planned: ${workout.distanceKm?.let { "%.1f".format(it) } ?: "?"} km). " +
            "Actual: $distStr, $durStr. RPE: $rpeStr. Feeling: $feelingStr. Notes: $notesStr. " +
            "Give 2-3 sentences of honest, practical coaching feedback. Be concise, no markdown."

        return ClaudeRequest(
            prompt = prompt,
            systemPrompt = "You are an experienced running coach. Give concise, honest, actionable post-workout feedback. Plain text only, no markdown, max 80 words.",
            maxTokens = 256
        )
    }
}
