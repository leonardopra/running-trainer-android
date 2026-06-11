package com.leopra.runningtrainer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plans")
data class TrainingPlanEntity(
    @PrimaryKey val id: String,
    val createdAtEpochMillis: Long,
    val payloadJson: String
)
