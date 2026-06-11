package com.leopra.runningtrainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSettingsStore(
    context: Context,
    private val apiKeyCipher: ApiKeyCipher
) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("running_trainer_settings.preferences_pb") }
    )

    val preferences: Flow<UserPreferencesDto> = dataStore.data.map { prefs ->
        UserPreferencesDto(
            // Encrypted slot first; fall back to the legacy plaintext slot (pre-RUN-48),
            // which is migrated to the encrypted slot on the next save.
            claudeApiKey = prefs[Keys.CLAUDE_API_KEY_ENCRYPTED]?.let(apiKeyCipher::decrypt)
                ?: prefs[Keys.CLAUDE_API_KEY],
            useKilometers = prefs[Keys.USE_KILOMETERS] ?: true,
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            name = prefs[Keys.NAME],
            age = prefs[Keys.AGE],
            weightKg = prefs[Keys.WEIGHT_KG],
            heightCm = prefs[Keys.HEIGHT_CM],
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false,
            notificationHour = prefs[Keys.NOTIFICATION_HOUR] ?: 8,
            notificationMinute = prefs[Keys.NOTIFICATION_MINUTE] ?: 0,
            goalTimeSeconds = prefs[Keys.GOAL_TIME_SECONDS],
            localeCode = prefs[Keys.LOCALE_CODE]
                ?: context.resources.configuration.locales[0].language
                    .takeIf { it in setOf("en", "it", "de") }
                ?: "en"
        )
    }

    suspend fun savePreferences(preferences: UserPreferencesDto) {
        dataStore.edit { prefs ->
            prefs[Keys.USE_KILOMETERS] = preferences.useKilometers
            prefs[Keys.HAS_COMPLETED_ONBOARDING] = preferences.hasCompletedOnboarding
            prefs[Keys.NOTIFICATIONS_ENABLED] = preferences.notificationsEnabled
            prefs[Keys.NOTIFICATION_HOUR] = preferences.notificationHour
            prefs[Keys.NOTIFICATION_MINUTE] = preferences.notificationMinute
            prefs[Keys.LOCALE_CODE] = preferences.localeCode

            writeOptional(prefs, Keys.CLAUDE_API_KEY_ENCRYPTED, preferences.claudeApiKey?.let(apiKeyCipher::encrypt))
            prefs.remove(Keys.CLAUDE_API_KEY)
            writeOptional(prefs, Keys.NAME, preferences.name)
            writeOptional(prefs, Keys.AGE, preferences.age)
            writeOptional(prefs, Keys.WEIGHT_KG, preferences.weightKg)
            writeOptional(prefs, Keys.HEIGHT_CM, preferences.heightCm)
            writeOptional(prefs, Keys.GOAL_TIME_SECONDS, preferences.goalTimeSeconds)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    private fun <T> writeOptional(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<T>,
        value: T?
    ) {
        if (value == null) {
            preferences.remove(key)
        } else {
            preferences[key] = value
        }
    }

    private object Keys {
        /** Legacy plaintext slot (pre-RUN-48); read-only fallback, removed on save. */
        val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
        val CLAUDE_API_KEY_ENCRYPTED = stringPreferencesKey("claude_api_key_enc")
        val USE_KILOMETERS = booleanPreferencesKey("use_kilometers")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val NAME = stringPreferencesKey("name")
        val AGE = intPreferencesKey("age")
        val WEIGHT_KG = doublePreferencesKey("weight_kg")
        val HEIGHT_CM = doublePreferencesKey("height_cm")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val GOAL_TIME_SECONDS = intPreferencesKey("goal_time_seconds")
        val LOCALE_CODE = stringPreferencesKey("locale_code")
    }
}
