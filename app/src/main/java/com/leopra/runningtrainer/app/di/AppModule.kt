package com.leopra.runningtrainer.app.di

import android.content.Context
import androidx.room.Room
import com.leopra.runningtrainer.data.local.ApiKeyCipher
import com.leopra.runningtrainer.data.local.AppDatabase
import com.leopra.runningtrainer.data.local.LocalSettingsStore
import com.leopra.runningtrainer.data.repository.LocalSettingsRepository
import com.leopra.runningtrainer.data.repository.LocalTrainingPlanRepository
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.data.repository.TrainingPlanRepository
import com.leopra.runningtrainer.domain.service.ClaudeService
import com.leopra.runningtrainer.domain.service.InsightsService
import com.leopra.runningtrainer.domain.service.PaceCalculatorService
import com.leopra.runningtrainer.domain.service.PlanGenerator
import com.leopra.runningtrainer.notifications.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "running_trainer_android.db").build()

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides @Singleton
    fun provideApiKeyCipher(): ApiKeyCipher = ApiKeyCipher()

    @Provides @Singleton
    fun provideLocalSettingsStore(
        @ApplicationContext context: Context,
        apiKeyCipher: ApiKeyCipher
    ): LocalSettingsStore = LocalSettingsStore(context, apiKeyCipher)

    @Provides @Singleton
    fun provideSettingsRepository(store: LocalSettingsStore): SettingsRepository =
        LocalSettingsRepository(store)

    @Provides @Singleton
    fun provideTrainingPlanRepository(db: AppDatabase, json: Json): TrainingPlanRepository =
        LocalTrainingPlanRepository(dao = db.trainingPlanDao(), generator = PlanGenerator(), json = json)

    @Provides @Singleton
    fun providePaceCalculatorService(): PaceCalculatorService = PaceCalculatorService()

    @Provides @Singleton
    fun provideInsightsService(): InsightsService = InsightsService()

    @Provides @Singleton
    fun provideClaudeService(): ClaudeService = ClaudeService()

    @Provides @Singleton
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService =
        NotificationService(context)
}
