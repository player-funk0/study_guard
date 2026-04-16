package com.obrynex.studyguard.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("studyguard_prefs")

object PrefsManager {

    private val DAILY_GOAL_MIN    = intPreferencesKey("daily_goal_min")
    private val SCREEN_LIMIT_HR   = floatPreferencesKey("screen_limit_hr")
    private val REMINDER_HOUR     = intPreferencesKey("reminder_hour")
    private val REMINDER_MINUTE   = intPreferencesKey("reminder_minute")
    private val REMINDER_ENABLED  = booleanPreferencesKey("reminder_enabled")
    private val ONBOARDING_DONE   = booleanPreferencesKey("onboarding_done")

    const val DEFAULT_GOAL_MIN  = 60
    const val DEFAULT_SCREEN_HR = 4f
    const val DEFAULT_REMIND_H  = 20
    const val DEFAULT_REMIND_M  = 0

    fun dailyGoalMin(ctx: Context): Flow<Int> =
        ctx.dataStore.data.map { it[DAILY_GOAL_MIN] ?: DEFAULT_GOAL_MIN }

    fun screenLimitHr(ctx: Context): Flow<Float> =
        ctx.dataStore.data.map { it[SCREEN_LIMIT_HR] ?: DEFAULT_SCREEN_HR }

    fun reminderEnabled(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[REMINDER_ENABLED] ?: true }

    fun reminderTime(ctx: Context): Flow<Pair<Int, Int>> =
        ctx.dataStore.data.map {
            (it[REMINDER_HOUR] ?: DEFAULT_REMIND_H) to (it[REMINDER_MINUTE] ?: DEFAULT_REMIND_M)
        }

    /** Returns true when the onboarding flow has been completed at least once. */
    fun onboardingDone(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    suspend fun setDailyGoal(ctx: Context, min: Int) =
        ctx.dataStore.edit { it[DAILY_GOAL_MIN] = min }

    suspend fun setScreenLimit(ctx: Context, hr: Float) =
        ctx.dataStore.edit { it[SCREEN_LIMIT_HR] = hr }

    suspend fun setReminderEnabled(ctx: Context, on: Boolean) =
        ctx.dataStore.edit { it[REMINDER_ENABLED] = on }

    suspend fun setReminderTime(ctx: Context, hour: Int, minute: Int) {
        ctx.dataStore.edit {
            it[REMINDER_HOUR]   = hour
            it[REMINDER_MINUTE] = minute
        }
    }

    suspend fun setOnboardingDone(ctx: Context) =
        ctx.dataStore.edit { it[ONBOARDING_DONE] = true }
}
