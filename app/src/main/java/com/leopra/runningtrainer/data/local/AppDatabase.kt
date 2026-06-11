package com.leopra.runningtrainer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrainingPlanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingPlanDao(): TrainingPlanDao
}
