package com.example.tree.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {
     val USER_KEY = stringPreferencesKey("user")
     val GOAL_KEY = intPreferencesKey("goal")

    // === 新增：每日步数持久化所需 ===
     val DAILY_STEPS_KEY = intPreferencesKey("daily_steps")
     val LAST_TOTAL_STEPS_KEY = longPreferencesKey("last_total_steps")
     val LAST_DATE_KEY = stringPreferencesKey("last_date")

    fun goalFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GOAL_KEY] ?: 10000
    }

    fun dailyStepsFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_STEPS_KEY] ?: 0
    }

    suspend fun saveUser(context: Context, user: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_KEY] = user
        }
    }

    suspend fun saveGoal(context: Context, goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[GOAL_KEY] = goal
        }
    }

    suspend fun resetTree(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(DAILY_STEPS_KEY)
            prefs.remove(LAST_TOTAL_STEPS_KEY)
            prefs.remove(LAST_DATE_KEY)
        }
    }
}