package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.model.CoachingInsight
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.InsightKind
import com.leopra.runningtrainer.domain.model.InsightType
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class InsightsService {

    fun generate(plan: TrainingPlan, today: LocalDate): List<CoachingInsight> {
        val insights = mutableListOf<CoachingInsight>()

        val daysSinceStart = ChronoUnit.DAYS.between(plan.startDate, today).toInt()
        val currentWeekIndex = daysSinceStart.div(7).coerceIn(0, plan.totalWeeks - 1)
        val currentWeek = plan.weeks[currentWeekIndex]

        // 1. Race countdown
        if (plan.raceDate != null) {
            val daysToRace = ChronoUnit.DAYS.between(today, plan.raceDate).toInt()
            if (daysToRace >= 0) insights.add(raceCountdown(daysToRace, plan.goalType))
        }

        // 2. Taper week
        if (currentWeek.isTaperWeek) {
            insights.add(CoachingInsight(
                id = "taper_week",
                kind = InsightKind.TAPER_WEEK,
                type = InsightType.INFO,
                priority = 5
            ))
        }

        // 3. Recovery week (volume drops ≥12% vs previous)
        if (!currentWeek.isTaperWeek && currentWeekIndex > 0) {
            val prevKm = plan.weeks[currentWeekIndex - 1].targetWeeklyKm
            val thisKm = currentWeek.targetWeeklyKm
            if (prevKm > 0 && thisKm / prevKm < 0.88) {
                insights.add(CoachingInsight(
                    id = "recovery_week",
                    kind = InsightKind.RECOVERY_WEEK,
                    type = InsightType.INFO,
                    priority = 6
                ))
            }
        }

        // 4. First week welcome
        if (currentWeekIndex == 0 && daysSinceStart < 7) {
            insights.add(CoachingInsight(
                id = "week_1_welcome",
                kind = InsightKind.WEEK_1_WELCOME,
                type = InsightType.MOTIVATION,
                priority = 4
            ))
        }

        // 5. Overall completion rate
        val pastWorkouts = (0 until currentWeekIndex).flatMap { wi ->
            plan.weeks[wi].workouts.filter { it.type != WorkoutType.rest }
        }
        if (pastWorkouts.isNotEmpty()) {
            val done = pastWorkouts.count { it.isCompleted }
            val rate = done.toDouble() / pastWorkouts.size
            when {
                rate >= 0.85 -> insights.add(CoachingInsight(
                    id = "high_consistency",
                    kind = InsightKind.HIGH_CONSISTENCY,
                    type = InsightType.POSITIVE,
                    priority = 10,
                    count = (rate * 100).toInt()
                ))
                rate < 0.55 && currentWeekIndex >= 2 -> insights.add(CoachingInsight(
                    id = "low_consistency",
                    kind = InsightKind.LOW_CONSISTENCY,
                    type = InsightType.WARNING,
                    priority = 8,
                    count = (rate * 100).toInt()
                ))
            }
        }

        // 6. Recent missed workouts (last 7 days)
        var recentMissed = 0
        for (d in 1..7) {
            val date = today.minusDays(d.toLong())
            val ds = ChronoUnit.DAYS.between(plan.startDate, date).toInt()
            if (ds < 0) break
            val wi = ds / 7
            if (wi >= plan.weeks.size) continue
            val dow = date.dayOfWeek.value
            val w = plan.weeks[wi].workouts.firstOrNull { it.dayOfWeek == dow && it.type != WorkoutType.rest }
            if (w != null && !w.isCompleted) recentMissed++
        }
        if (recentMissed >= 3) {
            insights.add(CoachingInsight(
                id = "back_on_track",
                kind = InsightKind.BACK_ON_TRACK,
                type = InsightType.WARNING,
                priority = 7,
                count = recentMissed
            ))
        }

        // 7. Current week volume progress
        val weekLoggedKm = currentWeek.workouts
            .filter { it.isCompleted && it.type != WorkoutType.rest }
            .sumOf { it.actualDistanceKm ?: it.distanceKm ?: 0.0 }
        val weekTargetKm = currentWeek.targetWeeklyKm
        val todayDow = today.dayOfWeek.value
        val plannedSoFar = currentWeek.workouts
            .filter { it.dayOfWeek <= todayDow && it.type != WorkoutType.rest }
            .sumOf { it.distanceKm ?: 0.0 }

        if (plannedSoFar > 0) {
            val weekRate = weekLoggedKm / plannedSoFar
            when {
                weekRate >= 1.0 && todayDow >= 3 -> insights.add(CoachingInsight(
                    id = "on_track",
                    kind = InsightKind.ON_TRACK_WEEK,
                    type = InsightType.POSITIVE,
                    priority = 12,
                    km = weekLoggedKm
                ))
                weekRate < 0.4 && todayDow >= 4 -> {
                    val remaining = (weekTargetKm - weekLoggedKm).coerceAtLeast(0.0)
                    insights.add(CoachingInsight(
                        id = "behind_this_week",
                        kind = InsightKind.BEHIND_WEEK,
                        type = InsightType.WARNING,
                        priority = 9,
                        km = remaining
                    ))
                }
            }
        }

        // 8. Easy runs paced too fast
        val loggedEasyRuns = plan.weeks
            .flatMap { it.workouts }
            .filter {
                it.type == WorkoutType.easyRun &&
                it.isCompleted &&
                (it.actualDistanceKm ?: 0.0) > 0.0 &&
                it.actualDurationMinutes != null &&
                it.durationMinutes != null &&
                (it.distanceKm ?: 0.0) > 0.0
            }
        if (loggedEasyRuns.size >= 3) {
            val tooFast = loggedEasyRuns.count { w ->
                val targetPace = (w.durationMinutes!! * 60).toDouble() / w.distanceKm!!
                val actualPace = (w.actualDurationMinutes!! * 60).toDouble() / w.actualDistanceKm!!
                actualPace < targetPace * 0.92
            }
            if (tooFast.toDouble() / loggedEasyRuns.size >= 0.6) {
                insights.add(CoachingInsight(
                    id = "easy_runs_too_fast",
                    kind = InsightKind.EASY_RUNS_TOO_FAST,
                    type = InsightType.WARNING,
                    priority = 11
                ))
            }
        }

        // 9. Easy run RPE too high
        val recentEasyWithRpe = plan.weeks
            .flatMap { it.workouts }
            .filter { w ->
                w.type == WorkoutType.easyRun &&
                w.isCompleted &&
                w.rpe != null &&
                w.completedAt != null &&
                ChronoUnit.DAYS.between(instantToLocalDate(w.completedAt), today) <= 14
            }
        if (recentEasyWithRpe.size >= 3 && recentEasyWithRpe.count { it.rpe!! >= 7 } >= 3) {
            insights.add(CoachingInsight(
                id = "high_rpe_easy",
                kind = InsightKind.HIGH_RPE_EASY,
                type = InsightType.WARNING,
                priority = 11
            ))
        }

        // 10. Consecutive tired/injured feeling
        val completedByDate = plan.weeks
            .flatMap { it.workouts }
            .filter { it.isCompleted && it.feeling != null && it.type != WorkoutType.rest && it.completedAt != null }
            .sortedByDescending { it.completedAt }
        if (completedByDate.size >= 2) {
            var consecutiveNeg = 0
            for (w in completedByDate) {
                if (w.feeling == WorkoutFeeling.tired || w.feeling == WorkoutFeeling.injured) {
                    consecutiveNeg++
                } else break
            }
            if (consecutiveNeg >= 2) {
                insights.add(CoachingInsight(
                    id = "negative_feeling",
                    kind = InsightKind.FATIGUE_SIGNS,
                    type = InsightType.WARNING,
                    priority = 9,
                    count = consecutiveNeg
                ))
            }
        }

        // 11. Long run skipped last week
        if (currentWeekIndex > 0) {
            val prevWeek = plan.weeks[currentWeekIndex - 1]
            val longRun = prevWeek.workouts.firstOrNull { it.type == WorkoutType.longRun }
            if (longRun != null && !longRun.isCompleted) {
                insights.add(CoachingInsight(
                    id = "missed_long_run",
                    kind = InsightKind.MISSED_LONG_RUN,
                    type = InsightType.WARNING,
                    priority = 8
                ))
            }
        }

        // 12. Streak celebration ≥5 days
        var streak = 0
        for (d in 0..29) {
            val date = today.minusDays(d.toLong())
            val ds = ChronoUnit.DAYS.between(plan.startDate, date).toInt()
            if (ds < 0) break
            val wi = ds / 7
            if (wi >= plan.weeks.size) break
            val dow = date.dayOfWeek.value
            val w = plan.weeks[wi].workouts.firstOrNull { it.dayOfWeek == dow && it.type != WorkoutType.rest }
            if (w == null) continue // rest day, doesn't break streak
            if (!w.isCompleted) break
            streak++
        }
        if (streak >= 5) {
            insights.add(CoachingInsight(
                id = "streak_$streak",
                kind = InsightKind.STREAK,
                type = InsightType.POSITIVE,
                priority = 13,
                count = streak
            ))
        }

        // 13. Tomorrow has a key session
        val tomorrow = today.plusDays(1)
        val tDs = ChronoUnit.DAYS.between(plan.startDate, tomorrow).toInt()
        if (tDs >= 0) {
            val tWi = tDs / 7
            if (tWi < plan.weeks.size) {
                val tDow = tomorrow.dayOfWeek.value
                val tomorrowWorkout = plan.weeks[tWi].workouts.firstOrNull { it.dayOfWeek == tDow }
                if (tomorrowWorkout != null && tomorrowWorkout.type in listOf(
                        WorkoutType.longRun, WorkoutType.intervalRun, WorkoutType.tempoRun
                    )
                ) {
                    insights.add(CoachingInsight(
                        id = "key_tomorrow",
                        kind = InsightKind.KEY_SESSION_TOMORROW,
                        type = InsightType.MOTIVATION,
                        priority = 14,
                        km = tomorrowWorkout.distanceKm,
                        workoutType = tomorrowWorkout.type
                    ))
                }
            }
        }

        return insights.sortedBy { it.priority }
    }

    private fun instantToLocalDate(instant: Instant): LocalDate {
        val kd = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return LocalDate.of(kd.year, kd.monthNumber, kd.dayOfMonth)
    }

    private fun raceCountdown(daysToRace: Int, goal: GoalType): CoachingInsight = when {
        daysToRace == 0 -> CoachingInsight(
            id = "race_day",
            kind = InsightKind.RACE_DAY,
            type = InsightType.MOTIVATION,
            priority = 1,
            goalType = goal
        )
        daysToRace <= 7 -> CoachingInsight(
            id = "race_week",
            kind = InsightKind.DAYS_TO_RACE,
            type = InsightType.MOTIVATION,
            priority = 2,
            count = daysToRace,
            goalType = goal
        )
        daysToRace <= 21 -> CoachingInsight(
            id = "almost_there",
            kind = InsightKind.WEEKS_LEFT_SOON,
            type = InsightType.INFO,
            priority = 3,
            count = (daysToRace + 6) / 7,
            goalType = goal
        )
        else -> CoachingInsight(
            id = "weeks_to_go",
            kind = InsightKind.WEEKS_TO_RACE,
            type = InsightType.INFO,
            priority = 15,
            count = (daysToRace + 6) / 7,
            goalType = goal
        )
    }
}
