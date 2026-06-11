package com.leopra.runningtrainer.data.repository

import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.contracts.PlanGenerationResult
import com.leopra.runningtrainer.domain.model.ProgressStats
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.domain.model.WorkoutLogInput
import kotlinx.coroutines.flow.Flow

interface TrainingPlanRepository {
    fun observeActivePlan(): Flow<TrainingPlan?>
    fun observeProgressStats(): Flow<ProgressStats?>
    suspend fun generateAndSavePlan(request: PlanGenerationRequest): PlanGenerationResult
    suspend fun updatePlan(plan: TrainingPlan)
    suspend fun saveWorkoutLog(input: WorkoutLogInput)
    suspend fun applyPostWorkoutCoaching(workoutId: String, coaching: String)
    suspend fun clearWorkoutLog(workoutId: String)
    suspend fun clearAllPlans()
}

interface SettingsRepository {
    fun observePreferences(): Flow<UserPreferencesDto>
    suspend fun savePreferences(preferences: UserPreferencesDto)
    suspend fun clear()
}
