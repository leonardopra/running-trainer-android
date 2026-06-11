package com.leopra.runningtrainer.data.serialization

import com.leopra.runningtrainer.domain.model.EffortLevel
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.TrainingWeek
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class SerializableTrainingPlan(
    val id: String,
    val goalType: GoalType,
    val fitnessLevel: FitnessLevel,
    val startDate: String,
    val raceDate: String? = null,
    val totalWeeks: Int,
    val trainingDaysPerWeek: Int,
    val weeks: List<SerializableTrainingWeek>,
    val createdAt: String,
    val isClaudeEnriched: Boolean
)

@Serializable
data class SerializableTrainingWeek(
    val weekNumber: Int,
    val weekTheme: String,
    val targetWeeklyKm: Double,
    val isTaperWeek: Boolean,
    val workouts: List<SerializableWorkout>
)

@Serializable
data class SerializableWorkout(
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
    val completedAt: String? = null,
    val notes: String? = null,
    val rpe: Int? = null,
    val feeling: WorkoutFeeling? = null,
    val postWorkoutCoaching: String? = null
)

fun TrainingPlan.toSerializable(): SerializableTrainingPlan = SerializableTrainingPlan(
    id = id,
    goalType = goalType,
    fitnessLevel = fitnessLevel,
    startDate = startDate.toString(),
    raceDate = raceDate?.toString(),
    totalWeeks = totalWeeks,
    trainingDaysPerWeek = trainingDaysPerWeek,
    weeks = weeks.map { it.toSerializable() },
    createdAt = createdAt.toString(),
    isClaudeEnriched = isClaudeEnriched
)

fun SerializableTrainingPlan.toDomain(): TrainingPlan = TrainingPlan(
    id = id,
    goalType = goalType,
    fitnessLevel = fitnessLevel,
    startDate = LocalDate.parse(startDate),
    raceDate = raceDate?.let(LocalDate::parse),
    totalWeeks = totalWeeks,
    trainingDaysPerWeek = trainingDaysPerWeek,
    weeks = weeks.map { it.toDomain() },
    createdAt = Instant.parse(createdAt),
    isClaudeEnriched = isClaudeEnriched
)

private fun TrainingWeek.toSerializable(): SerializableTrainingWeek = SerializableTrainingWeek(
    weekNumber = weekNumber,
    weekTheme = weekTheme,
    targetWeeklyKm = targetWeeklyKm,
    isTaperWeek = isTaperWeek,
    workouts = workouts.map { it.toSerializable() }
)

private fun SerializableTrainingWeek.toDomain(): TrainingWeek = TrainingWeek(
    weekNumber = weekNumber,
    weekTheme = weekTheme,
    targetWeeklyKm = targetWeeklyKm,
    isTaperWeek = isTaperWeek,
    workouts = workouts.map { it.toDomain() }
)

private fun Workout.toSerializable(): SerializableWorkout = SerializableWorkout(
    id = id,
    type = type,
    dayOfWeek = dayOfWeek,
    distanceKm = distanceKm,
    durationMinutes = durationMinutes,
    effortLevel = effortLevel,
    title = title,
    description = description,
    coachingTip = coachingTip,
    isCompleted = isCompleted,
    actualDistanceKm = actualDistanceKm,
    actualDurationMinutes = actualDurationMinutes,
    completedAt = completedAt?.toString(),
    notes = notes,
    rpe = rpe,
    feeling = feeling,
    postWorkoutCoaching = postWorkoutCoaching
)

private fun SerializableWorkout.toDomain(): Workout = Workout(
    id = id,
    type = type,
    dayOfWeek = dayOfWeek,
    distanceKm = distanceKm,
    durationMinutes = durationMinutes,
    effortLevel = effortLevel,
    title = title,
    description = description,
    coachingTip = coachingTip,
    isCompleted = isCompleted,
    actualDistanceKm = actualDistanceKm,
    actualDurationMinutes = actualDurationMinutes,
    completedAt = completedAt?.let(Instant::parse),
    notes = notes,
    rpe = rpe,
    feeling = feeling,
    postWorkoutCoaching = postWorkoutCoaching
)
