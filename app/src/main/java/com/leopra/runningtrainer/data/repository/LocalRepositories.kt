package com.leopra.runningtrainer.data.repository

import com.leopra.runningtrainer.data.local.LocalSettingsStore
import com.leopra.runningtrainer.data.local.TrainingPlanDao
import com.leopra.runningtrainer.data.local.TrainingPlanEntity
import com.leopra.runningtrainer.data.serialization.SerializableTrainingPlan
import com.leopra.runningtrainer.data.serialization.toDomain
import com.leopra.runningtrainer.data.serialization.toSerializable
import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.contracts.PlanGenerationResult
import com.leopra.runningtrainer.domain.model.ProgressStats
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.domain.model.WorkoutLogInput
import com.leopra.runningtrainer.domain.service.PlanGenerator
import com.leopra.runningtrainer.domain.service.ProgressStatsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class LocalTrainingPlanRepository(
    private val dao: TrainingPlanDao,
    private val generator: PlanGenerator,
    private val json: Json,
    private val progressStatsCalculator: ProgressStatsCalculator = ProgressStatsCalculator(Clock.System)
) : TrainingPlanRepository {
    override fun observeActivePlan(): Flow<TrainingPlan?> = dao.observeActive().map { entity ->
        entity?.let(::decodeEntity)
    }

    override fun observeProgressStats(): Flow<ProgressStats?> = observeActivePlan().map { plan ->
        plan?.let(progressStatsCalculator::compute)
    }

    override suspend fun generateAndSavePlan(request: PlanGenerationRequest): PlanGenerationResult {
        val result = generator.generatePlan(request)
        persistPlan(result.plan)
        return result
    }

    override suspend fun updatePlan(plan: TrainingPlan) {
        persistPlan(plan)
    }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput) {
        updateActivePlan { plan ->
            plan.copy(
                weeks = plan.weeks.map { week ->
                    week.copy(
                        workouts = week.workouts.map { workout ->
                            if (workout.id != input.workoutId) {
                                workout
                            } else {
                                workout.copy(
                                    isCompleted = input.isCompleted,
                                    actualDistanceKm = input.actualDistanceKm,
                                    actualDurationMinutes = input.actualDurationMinutes,
                                    notes = input.notes?.takeIf { it.isNotBlank() },
                                    rpe = input.rpe,
                                    feeling = input.feeling,
                                    completedAt = input.completedAt ?: workout.completedAt ?: Clock.System.now()
                                )
                            }
                        }
                    )
                }
            )
        }
    }

    override suspend fun applyPostWorkoutCoaching(workoutId: String, coaching: String) {
        updateActivePlan { plan ->
            plan.copy(
                weeks = plan.weeks.map { week ->
                    week.copy(
                        workouts = week.workouts.map { workout ->
                            if (workout.id == workoutId) workout.copy(postWorkoutCoaching = coaching)
                            else workout
                        }
                    )
                }
            )
        }
    }

    override suspend fun clearWorkoutLog(workoutId: String) {
        updateActivePlan { plan ->
            plan.copy(
                weeks = plan.weeks.map { week ->
                    week.copy(
                        workouts = week.workouts.map { workout ->
                            if (workout.id != workoutId) {
                                workout
                            } else {
                                workout.copy(
                                    isCompleted = false,
                                    actualDistanceKm = null,
                                    actualDurationMinutes = null,
                                    completedAt = null,
                                    notes = null,
                                    rpe = null,
                                    feeling = null,
                                    postWorkoutCoaching = null
                                )
                            }
                        }
                    )
                }
            )
        }
    }

    override suspend fun clearAllPlans() {
        dao.deleteAll()
    }

    private suspend fun updateActivePlan(transform: (TrainingPlan) -> TrainingPlan) {
        val active = dao.observeActiveOnce() ?: return
        val updated = transform(decodeEntity(active))
        persistPlan(updated)
    }

    private suspend fun persistPlan(plan: TrainingPlan) {
        val payload = json.encodeToString(SerializableTrainingPlan.serializer(), plan.toSerializable())
        dao.upsert(
            TrainingPlanEntity(
                id = plan.id,
                createdAtEpochMillis = plan.createdAt.toEpochMilliseconds(),
                payloadJson = payload
            )
        )
    }

    private fun decodeEntity(entity: TrainingPlanEntity): TrainingPlan {
        val serializable = json.decodeFromString(SerializableTrainingPlan.serializer(), entity.payloadJson)
        return serializable.toDomain()
    }
}

class LocalSettingsRepository(
    private val store: LocalSettingsStore
) : SettingsRepository {
    override fun observePreferences(): Flow<UserPreferencesDto> = store.preferences

    override suspend fun savePreferences(preferences: UserPreferencesDto) {
        store.savePreferences(preferences)
    }

    override suspend fun clear() {
        store.clearAll()
    }
}
