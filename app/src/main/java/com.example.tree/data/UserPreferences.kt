package com.example.tree.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val USER_KEY = stringPreferencesKey("user")
    private val GOAL_KEY = intPreferencesKey("goal")

    fun goalFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GOAL_KEY] ?: 5000
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
}