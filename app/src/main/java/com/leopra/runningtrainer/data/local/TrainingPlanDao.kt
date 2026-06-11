package com.leopra.runningtrainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    @Query("SELECT * FROM training_plans ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<TrainingPlanEntity>>

    @Query("SELECT * FROM training_plans ORDER BY createdAtEpochMillis DESC LIMIT 1")
    fun observeActive(): Flow<TrainingPlanEntity?>

    @Query("SELECT * FROM training_plans ORDER BY createdAtEpochMillis DESC LIMIT 1")
    suspend fun observeActiveOnce(): TrainingPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: TrainingPlanEntity)

    @Query("DELETE FROM training_plans")
    suspend fun deleteAll()
}
