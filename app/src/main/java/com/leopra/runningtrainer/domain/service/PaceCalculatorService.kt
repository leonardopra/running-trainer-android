package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.PaceZone
import com.leopra.runningtrainer.domain.model.WorkoutType

class PaceCalculatorService {

    fun calculate(goal: GoalType, goalTimeSeconds: Int): List<PaceZone> {
        if (goalTimeSeconds < 600 || goalTimeSeconds > 36000) return emptyList()

        val racePace = goalTimeSeconds.toDouble() / distanceKm(goal)
        val goalZones = ZONES[goal] ?: ZONES[GoalType.tenK]!!

        return DISPLAY_ORDER.mapNotNull { type ->
            val (fast, slow) = goalZones[type] ?: return@mapNotNull null
            PaceZone(
                type = type,
                fastSecs = (racePace * fast).toInt(),
                slowSecs = (racePace * slow).toInt(),
                description = DESCRIPTIONS[type] ?: ""
            )
        }
    }

    fun formatGoalTime(totalSecs: Int): String {
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        return if (h > 0) "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        else "$m:${s.toString().padStart(2, '0')}"
    }

    companion object {
        private data class ZoneMult(val fast: Double, val slow: Double)

        fun distanceKm(goal: GoalType): Double = when (goal) {
            GoalType.fiveK -> 5.0
            GoalType.tenK -> 10.0
            GoalType.halfMarathon -> 21.0975
            GoalType.marathon -> 42.195
            GoalType.trailRun -> 25.0
            GoalType.generalFitness -> 10.0
        }

        private val ZONES: Map<GoalType, Map<WorkoutType, ZoneMult>> = mapOf(
            GoalType.fiveK to mapOf(
                WorkoutType.easyRun to ZoneMult(1.30, 1.43),
                WorkoutType.longRun to ZoneMult(1.33, 1.46),
                WorkoutType.tempoRun to ZoneMult(1.06, 1.12),
                WorkoutType.intervalRun to ZoneMult(0.99, 1.03)
            ),
            GoalType.tenK to mapOf(
                WorkoutType.easyRun to ZoneMult(1.22, 1.34),
                WorkoutType.longRun to ZoneMult(1.25, 1.37),
                WorkoutType.tempoRun to ZoneMult(1.02, 1.08),
                WorkoutType.intervalRun to ZoneMult(0.94, 0.98)
            ),
            GoalType.halfMarathon to mapOf(
                WorkoutType.easyRun to ZoneMult(1.15, 1.26),
                WorkoutType.longRun to ZoneMult(1.17, 1.28),
                WorkoutType.tempoRun to ZoneMult(0.98, 1.04),
                WorkoutType.intervalRun to ZoneMult(0.88, 0.93)
            ),
            GoalType.marathon to mapOf(
                WorkoutType.easyRun to ZoneMult(1.12, 1.22),
                WorkoutType.longRun to ZoneMult(1.08, 1.17),
                WorkoutType.tempoRun to ZoneMult(0.93, 0.97),
                WorkoutType.intervalRun to ZoneMult(0.81, 0.86)
            ),
            GoalType.trailRun to mapOf(
                WorkoutType.easyRun to ZoneMult(1.18, 1.30),
                WorkoutType.longRun to ZoneMult(1.20, 1.33),
                WorkoutType.tempoRun to ZoneMult(0.97, 1.03),
                WorkoutType.intervalRun to ZoneMult(0.85, 0.90)
            ),
            GoalType.generalFitness to mapOf(
                WorkoutType.easyRun to ZoneMult(1.22, 1.34),
                WorkoutType.longRun to ZoneMult(1.25, 1.37),
                WorkoutType.tempoRun to ZoneMult(1.02, 1.08),
                WorkoutType.intervalRun to ZoneMult(0.94, 0.98)
            )
        )

        private val DESCRIPTIONS: Map<WorkoutType, String> = mapOf(
            WorkoutType.easyRun to "Conversational pace. Should feel easy — you could hold a full conversation. Builds aerobic base.",
            WorkoutType.longRun to "Slightly slower than easy. Used for your weekend long run to build endurance.",
            WorkoutType.tempoRun to "Comfortably hard. You can speak in short sentences. Raises lactate threshold.",
            WorkoutType.intervalRun to "Hard effort. Brief high-intensity bursts at or faster than race pace. Builds VO\u2082max."
        )

        val DISPLAY_ORDER = listOf(
            WorkoutType.easyRun,
            WorkoutType.longRun,
            WorkoutType.tempoRun,
            WorkoutType.intervalRun
        )
    }
}
