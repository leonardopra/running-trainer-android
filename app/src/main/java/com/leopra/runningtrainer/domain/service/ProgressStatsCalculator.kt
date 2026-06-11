package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.PaceDataPoint
import com.leopra.runningtrainer.domain.model.ProgressStats
import com.leopra.runningtrainer.domain.model.RpeDataPoint
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WeekProgress
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import com.leopra.runningtrainer.domain.model.WorkoutTypeCount
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ProgressStatsCalculator(
    private val clock: Clock = Clock.System
) {
    fun compute(plan: TrainingPlan): ProgressStats {
        val today = LocalDate.parse(clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())
        val daysSinceStart = ChronoUnit.DAYS.between(plan.startDate, today).toInt()
        val currentWeekIndex = (daysSinceStart / 7).coerceIn(0, plan.totalWeeks - 1)

        var totalNonRest = 0
        var completed = 0
        var totalPlanned = 0.0
        var totalLogged = 0.0
        val weeklyProgress = mutableListOf<WeekProgress>()
        val rpePoints = mutableListOf<RpeDataPoint>()
        val feelingMap = mutableMapOf<WorkoutFeeling, Int>()
        val workoutTypeMap = mutableMapOf<WorkoutType, Int>()

        plan.weeks.forEachIndexed { weekIndex, week ->
            val hasStarted = weekIndex <= currentWeekIndex
            var weekPlanned = 0.0
            var weekLogged = 0.0
            var weekNonRest = 0
            var weekCompleted = 0

            week.workouts.forEach { workout ->
                if (workout.type == WorkoutType.rest) return@forEach

                weekNonRest++
                totalNonRest++
                weekPlanned += workout.distanceKm ?: 0.0
                totalPlanned += workout.distanceKm ?: 0.0

                if (hasStarted) {
                    workoutTypeMap[workout.type] = (workoutTypeMap[workout.type] ?: 0) + 1
                }

                if (workout.isCompleted) {
                    weekCompleted++
                    completed++
                    val loggedKm = workout.actualDistanceKm ?: workout.distanceKm ?: 0.0
                    weekLogged += loggedKm
                    totalLogged += loggedKm
                    if (workout.rpe != null && workout.completedAt != null) {
                        rpePoints += RpeDataPoint(workout.completedAt, workout.rpe, workout.type)
                    }
                    if (workout.feeling != null) {
                        feelingMap[workout.feeling] = (feelingMap[workout.feeling] ?: 0) + 1
                    }
                }
            }

            if (hasStarted) {
                weeklyProgress += WeekProgress(
                    weekNumber = weekIndex + 1,
                    plannedKm = weekPlanned,
                    loggedKm = weekLogged,
                    totalWorkouts = weekNonRest,
                    completedWorkouts = weekCompleted,
                    hasStarted = true
                )
            }
        }

        val recentRpe = rpePoints.sortedBy { it.date }.takeLast(12)
        val allCompleted = plan.weeks
            .flatMap { it.workouts }
            .filter { it.isCompleted && it.type != WorkoutType.rest }
            .sortedBy { it.completedAt ?: Instant.fromEpochMilliseconds(0) }

        val pacePoints = allCompleted
            .filter { (it.actualDistanceKm ?: 0.0) > 0.0 && (it.actualDurationMinutes ?: 0) > 0 }
            .takeLast(12)
            .mapNotNull {
                val completedAt = it.completedAt ?: return@mapNotNull null
                PaceDataPoint(
                    paceMinPerKm = it.actualDurationMinutes!!.toDouble() / it.actualDistanceKm!!,
                    type = it.type,
                    date = completedAt
                )
            }

        val streak = computeStreak(plan, today)

        return ProgressStats(
            totalNonRestWorkouts = totalNonRest,
            completedWorkouts = completed,
            totalPlannedKm = totalPlanned,
            totalLoggedKm = totalLogged,
            currentStreak = streak,
            weeklyProgress = weeklyProgress,
            rpeDataPoints = recentRpe,
            feelingCounts = feelingMap,
            paceDataPoints = pacePoints,
            workoutTypeCounts = WorkoutType.entries
                .filter { it != WorkoutType.rest }
                .map { WorkoutTypeCount(it, workoutTypeMap[it] ?: 0) }
                .filter { it.count > 0 },
            recentCompletedWorkouts = allCompleted.takeLast(8).reversed()
        )
    }

    private fun computeStreak(plan: TrainingPlan, today: LocalDate): Int {
        var streak = 0
        for (offset in 0 downTo -365) {
            val date = today.plusDays(offset.toLong())
            val daysSince = ChronoUnit.DAYS.between(plan.startDate, date).toInt()
            if (daysSince < 0) break

            val weekIndex = daysSince / 7
            if (weekIndex >= plan.weeks.size) continue

            val dayOfWeek = date.dayOfWeek.value
            val workout = plan.weeks[weekIndex].workouts.firstOrNull { it.dayOfWeek == dayOfWeek }
            if (workout == null || workout.type == WorkoutType.rest) continue
            if (!workout.isCompleted) break
            streak++
        }
        return streak
    }
}
