package com.leopra.runningtrainer.domain.model

import kotlinx.datetime.Instant
import java.time.LocalDate

data class Workout(
    val id: String,
    val type: WorkoutType,
    val dayOfWeek: Int,
    val distanceKm: Double? = null,
    val durationMinutes: Int? = null,
    val effortLevel: EffortLevel,
    val title: String,
    val description: String? = null,
    val coachingTip: String? = null,
    val isCompleted: Boolean = false,
    val actualDistanceKm: Double? = null,
    val actualDurationMinutes: Int? = null,
    val completedAt: Instant? = null,
    val notes: String? = null,
    val rpe: Int? = null,
    val feeling: WorkoutFeeling? = null,
    val postWorkoutCoaching: String? = null
)

data class TrainingWeek(
    val weekNumber: Int,
    val weekTheme: String,
    val targetWeeklyKm: Double,
    val isTaperWeek: Boolean,
    val workouts: List<Workout>
)

data class TrainingPlan(
    val id: String,
    val goalType: GoalType,
    val fitnessLevel: FitnessLevel,
    val startDate: LocalDate,
    val raceDate: LocalDate? = null,
    val totalWeeks: Int,
    val trainingDaysPerWeek: Int,
    val weeks: List<TrainingWeek>,
    val createdAt: Instant,
    val isClaudeEnriched: Boolean = false
)

data class UserPreferencesDto(
    val claudeApiKey: String? = null,
    val useKilometers: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val name: String? = null,
    val age: Int? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val goalTimeSeconds: Int? = null,
    val localeCode: String = "en"
)

data class WorkoutLogInput(
    val workoutId: String,
    val isCompleted: Boolean,
    val actualDistanceKm: Double? = null,
    val actualDurationMinutes: Int? = null,
    val notes: String? = null,
    val rpe: Int? = null,
    val feeling: WorkoutFeeling? = null,
    val completedAt: Instant? = null
)

data class WorkoutTypeCount(
    val type: WorkoutType,
    val count: Int
)

data class PaceZone(
    val type: WorkoutType,
    val fastSecs: Int,
    val slowSecs: Int,
    val description: String
) {
    val label: String
        get() = when (type) {
            WorkoutType.easyRun -> "Easy Run"
            WorkoutType.longRun -> "Long Run"
            WorkoutType.tempoRun -> "Tempo Run"
            WorkoutType.intervalRun -> "Interval Run"
            else -> type.name
        }

    val paceRange: String
        get() = "${fmt(fastSecs)} – ${fmt(slowSecs)} /km"

    private fun fmt(secs: Int): String {
        val m = secs / 60
        val s = secs % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }
}

enum class InsightType { INFO, POSITIVE, WARNING, MOTIVATION }

enum class InsightKind {
    TAPER_WEEK, RECOVERY_WEEK, WEEK_1_WELCOME, HIGH_CONSISTENCY, LOW_CONSISTENCY,
    BACK_ON_TRACK, ON_TRACK_WEEK, BEHIND_WEEK, EASY_RUNS_TOO_FAST, HIGH_RPE_EASY,
    FATIGUE_SIGNS, MISSED_LONG_RUN, STREAK, KEY_SESSION_TOMORROW,
    RACE_DAY, DAYS_TO_RACE, WEEKS_LEFT_SOON, WEEKS_TO_RACE
}

// Display text is resolved from string resources in the UI layer (see titleText() in HomeScreen.kt);
// the domain only carries the kind plus the values needed to format it.
data class CoachingInsight(
    val id: String,
    val kind: InsightKind,
    val type: InsightType,
    val priority: Int,
    val count: Int? = null,
    val km: Double? = null,
    val goalType: GoalType? = null,
    val workoutType: WorkoutType? = null
)

data class PaceDataPoint(
    val paceMinPerKm: Double,
    val type: WorkoutType,
    val date: Instant
)

data class RpeDataPoint(
    val date: Instant,
    val rpe: Int,
    val type: WorkoutType
)

data class WeekProgress(
    val weekNumber: Int,
    val plannedKm: Double,
    val loggedKm: Double,
    val totalWorkouts: Int,
    val completedWorkouts: Int,
    val hasStarted: Boolean
) {
    val completionRate: Double
        get() = if (totalWorkouts == 0) 0.0 else completedWorkouts.toDouble() / totalWorkouts
}

data class ProgressStats(
    val totalNonRestWorkouts: Int,
    val completedWorkouts: Int,
    val totalPlannedKm: Double,
    val totalLoggedKm: Double,
    val currentStreak: Int,
    val weeklyProgress: List<WeekProgress>,
    val rpeDataPoints: List<RpeDataPoint>,
    val feelingCounts: Map<WorkoutFeeling, Int>,
    val paceDataPoints: List<PaceDataPoint> = emptyList(),
    val workoutTypeCounts: List<WorkoutTypeCount> = emptyList(),
    val recentCompletedWorkouts: List<Workout> = emptyList()
) {
    val completionRate: Double
        get() = if (totalNonRestWorkouts == 0) 0.0 else completedWorkouts.toDouble() / totalNonRestWorkouts

    val loggedRate: Double
        get() = if (totalPlannedKm == 0.0) 0.0 else totalLoggedKm / totalPlannedKm

    val weeklyKmHistory: List<Pair<String, Float>>
        get() = weeklyProgress.map { "W${it.weekNumber}" to it.loggedKm.toFloat() }
}
