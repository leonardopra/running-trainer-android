package com.leopra.runningtrainer.domain.service

import com.leopra.runningtrainer.domain.contracts.PlanGenerationMetadata
import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.contracts.PlanGenerationResult
import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.TrainingWeek
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.datetime.Clock
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class PlanGenerator(
    private val clock: Clock = Clock.System,
    private val idProvider: () -> String = { UUID.randomUUID().toString() }
) {
    private val baseMileage = mapOf(
        FitnessLevel.beginner to 20.0,
        FitnessLevel.intermediate to 35.0,
        FitnessLevel.advanced to 55.0
    )

    private val defaultWeeks = mapOf(
        GoalType.fiveK to 8,
        GoalType.tenK to 10,
        GoalType.halfMarathon to 12,
        GoalType.marathon to 16,
        GoalType.trailRun to 14,
        GoalType.generalFitness to 8
    )

    fun generatePlan(request: PlanGenerationRequest): PlanGenerationResult {
        val startDate = request.startDate ?: LocalDate.now()
        val recoveryInterval = if ((request.age ?: 0) >= 50) 3 else 4
        val progressionRate = if (recoveryInterval == 3) 1.07 else 1.09
        val totalWeeks = calculateTotalWeeks(
            goalType = request.goalType,
            raceDate = request.raceDate,
            durationWeeks = request.durationWeeks,
            startDate = startDate
        )
        val weeks = generateWeeks(
            goalType = request.goalType,
            fitnessLevel = request.fitnessLevel,
            trainingDaysPerWeek = request.trainingDaysPerWeek.coerceIn(3, 6),
            totalWeeks = totalWeeks,
            age = request.age
        )

        val plan = TrainingPlan(
            id = idProvider(),
            goalType = request.goalType,
            fitnessLevel = request.fitnessLevel,
            startDate = startDate,
            raceDate = request.raceDate,
            totalWeeks = totalWeeks,
            trainingDaysPerWeek = request.trainingDaysPerWeek.coerceIn(3, 6),
            weeks = weeks,
            createdAt = clock.now(),
            isClaudeEnriched = false
        )

        return PlanGenerationResult(
            plan = plan,
            metadata = PlanGenerationMetadata(
                recoveryIntervalWeeks = recoveryInterval,
                progressionRate = progressionRate,
                taperApplied = request.goalType != GoalType.generalFitness
            )
        )
    }

    private fun calculateTotalWeeks(
        goalType: GoalType,
        raceDate: LocalDate?,
        durationWeeks: Int?,
        startDate: LocalDate
    ): Int {
        if (durationWeeks != null) {
            return durationWeeks.coerceIn(4, 24)
        }
        if (raceDate != null) {
            val diffDays = ChronoUnit.DAYS.between(startDate, raceDate).toInt()
            return (diffDays / 7).coerceIn(4, 24)
        }
        return checkNotNull(defaultWeeks[goalType])
    }

    private fun generateWeeks(
        goalType: GoalType,
        fitnessLevel: FitnessLevel,
        trainingDaysPerWeek: Int,
        totalWeeks: Int,
        age: Int?
    ): List<TrainingWeek> {
        val recoveryInterval = if ((age ?: 0) >= 50) 3 else 4
        val mileageProgression = calculateMileageProgression(
            baseMileage = checkNotNull(baseMileage[fitnessLevel]),
            totalWeeks = totalWeeks,
            isRaceGoal = goalType != GoalType.generalFitness,
            recoveryInterval = recoveryInterval
        )

        return mileageProgression.mapIndexed { index, weeklyKm ->
            val weekNumber = index + 1
            val isRecovery = weekNumber % recoveryInterval == 0 && weekNumber < totalWeeks - 2
            val isTaper = goalType != GoalType.generalFitness && weekNumber > totalWeeks - 3

            TrainingWeek(
                weekNumber = weekNumber,
                weekTheme = weekTheme(
                    weekNumber = weekNumber,
                    totalWeeks = totalWeeks,
                    isRecovery = isRecovery,
                    isTaper = isTaper,
                    goalType = goalType,
                    age = age
                ),
                targetWeeklyKm = weeklyKm,
                isTaperWeek = isTaper,
                workouts = generateWorkoutsForWeek(
                    trainingDaysPerWeek = trainingDaysPerWeek,
                    weeklyKm = weeklyKm
                )
            )
        }
    }

    private fun calculateMileageProgression(
        baseMileage: Double,
        totalWeeks: Int,
        isRaceGoal: Boolean,
        recoveryInterval: Int
    ): List<Double> {
        val progression = mutableListOf<Double>()
        var current = baseMileage
        var peak = baseMileage
        val progressionRate = if (recoveryInterval == 3) 1.07 else 1.09

        repeat(totalWeeks) { index ->
            val weekNumber = index + 1
            val isTaper = isRaceGoal && weekNumber > totalWeeks - 3
            val isRecovery = weekNumber % recoveryInterval == 0 && weekNumber < totalWeeks - 2

            current = when {
                isTaper -> {
                    when (weekNumber - (totalWeeks - 3)) {
                        1 -> peak * 0.70
                        2 -> peak * 0.50
                        else -> peak * 0.30
                    }
                }

                isRecovery -> current * 0.80
                index > 0 && progression.isNotEmpty() -> progression.last() * progressionRate
                else -> current
            }

            if (!isTaper && !isRecovery && current > peak) {
                peak = current
            }

            progression += roundToSingleDecimal(current)
        }

        return progression
    }

    private fun weekTheme(
        weekNumber: Int,
        totalWeeks: Int,
        isRecovery: Boolean,
        isTaper: Boolean,
        goalType: GoalType,
        age: Int?
    ): String {
        if (weekNumber == 1) return "Foundation Week"
        if (isTaper) {
            return when (weekNumber - (totalWeeks - 3)) {
                1 -> "Taper Begins"
                2 -> "Race Prep"
                else -> "Race Week"
            }
        }
        if (isRecovery) {
            return if ((age ?: 0) >= 50) "Recovery Week (50+ protocol)" else "Recovery Week"
        }
        if (weekNumber <= totalWeeks * 0.4) return "Base Building"
        if (weekNumber <= totalWeeks * 0.7) return "Strength Phase"
        return "Peak Training"
    }

    private fun generateWorkoutsForWeek(
        trainingDaysPerWeek: Int,
        weeklyKm: Double
    ): List<Workout> {
        val distribution = workoutDistribution(trainingDaysPerWeek)
        val scaled = scaleWorkoutDistances(distribution, weeklyKm)
        return assignDaysOfWeek(scaled, trainingDaysPerWeek)
    }

    private fun workoutDistribution(days: Int): List<WorkoutType> = when (days) {
        3 -> listOf(WorkoutType.easyRun, WorkoutType.longRun, WorkoutType.easyRun)
        4 -> listOf(WorkoutType.easyRun, WorkoutType.tempoRun, WorkoutType.easyRun, WorkoutType.longRun)
        5 -> listOf(
            WorkoutType.easyRun,
            WorkoutType.easyRun,
            WorkoutType.tempoRun,
            WorkoutType.easyRun,
            WorkoutType.longRun
        )

        6 -> listOf(
            WorkoutType.easyRun,
            WorkoutType.easyRun,
            WorkoutType.tempoRun,
            WorkoutType.easyRun,
            WorkoutType.intervalRun,
            WorkoutType.longRun
        )

        else -> workoutDistribution(3)
    }

    private fun scaleWorkoutDistances(types: List<WorkoutType>, weeklyKm: Double): List<Pair<WorkoutType, Double>> {
        val weights = mapOf(
            WorkoutType.easyRun to 1.0,
            WorkoutType.tempoRun to 0.8,
            WorkoutType.intervalRun to 0.7,
            WorkoutType.longRun to 1.8
        )
        val totalWeight = types.sumOf { checkNotNull(weights[it]) }
        return types.map { type ->
            val distance = weeklyKm * checkNotNull(weights[type]) / totalWeight
            type to roundToSingleDecimal(distance)
        }
    }

    private fun assignDaysOfWeek(
        workouts: List<Pair<WorkoutType, Double>>,
        trainingDays: Int
    ): List<Workout> {
        val daysByCount = mapOf(
            3 to listOf(1, 3, 7),
            4 to listOf(1, 3, 5, 7),
            5 to listOf(1, 2, 4, 5, 7),
            6 to listOf(1, 2, 3, 5, 6, 7)
        )
        val scheduledDays = checkNotNull(daysByCount[trainingDays] ?: daysByCount[3])
        return (1..7).map { day ->
            val dayIndex = scheduledDays.indexOf(day)
            if (dayIndex >= 0 && dayIndex < workouts.size) {
                val (type, distance) = workouts[dayIndex]
                Workout(
                    id = idProvider(),
                    type = type,
                    dayOfWeek = day,
                    distanceKm = if (type == WorkoutType.rest) null else distance,
                    effortLevel = effortLevel(type),
                    title = workoutTitle(type, distance)
                )
            } else {
                Workout(
                    id = idProvider(),
                    type = WorkoutType.rest,
                    dayOfWeek = day,
                    effortLevel = EffortLevel.veryEasy,
                    title = "Rest Day"
                )
            }
        }
    }

    private fun effortLevel(type: WorkoutType): EffortLevel = when (type) {
        WorkoutType.easyRun -> EffortLevel.easy
        WorkoutType.tempoRun -> EffortLevel.hard
        WorkoutType.intervalRun -> EffortLevel.veryHard
        WorkoutType.longRun -> EffortLevel.moderate
        WorkoutType.rest -> EffortLevel.veryEasy
        WorkoutType.crossTrain -> EffortLevel.easy
    }

    private fun workoutTitle(type: WorkoutType, distanceKm: Double): String = when (type) {
        WorkoutType.easyRun -> "${formatKm(distanceKm)}km Easy Run"
        WorkoutType.tempoRun -> "${formatKm(distanceKm)}km Tempo Run"
        WorkoutType.intervalRun -> "Intervals (${formatKm(distanceKm)}km)"
        WorkoutType.longRun -> "${formatKm(distanceKm)}km Long Run"
        WorkoutType.rest -> "Rest Day"
        WorkoutType.crossTrain -> "Cross Training"
    }

    private fun roundToSingleDecimal(value: Double): Double =
        BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toDouble()

    private fun formatKm(value: Double): String = "%.1f".format(java.util.Locale.US, value)
}
