
package com.example.tree.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tree_prefs")

object UserPreferences {
    // ==================== Account Related ====================
    val EMAIL_KEY = stringPreferencesKey("current_user_email")  //
    val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")

    // Per-email password key for multi-account isolation
    fun passwordKey(email: String) = stringPreferencesKey("password_$email")

    // ==================== Dynamic Keys (Per-Email Independent Data) ====================
    fun dailyStepsKey(email: String) = intPreferencesKey("daily_steps_$email")
    fun goalKey(email: String) = intPreferencesKey("daily_goal_$email")
    fun historyStepsKey(email: String) = stringSetPreferencesKey("history_steps_$email")
    fun lastTotalStepsKey(email: String) = longPreferencesKey("last_total_steps_$email")
    fun lastDateKey(email: String) = stringPreferencesKey("last_date_$email")

    suspend fun getCurrentEmail(context: Context): String {
        return context.dataStore.data.first()[EMAIL_KEY] ?: ""
    }

    // ==================== Flows ====================
    fun dailyStepsFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        val email = prefs[EMAIL_KEY] ?: return@map 0
        prefs[dailyStepsKey(email)] ?: 0
    }

    fun goalFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        val email = prefs[EMAIL_KEY] ?: return@map 10000
        prefs[goalKey(email)] ?: 10000
    }

    fun historyStepsFlow(context: Context): Flow<List<Pair<String, Int>>> = context.dataStore.data.map { prefs ->
        val email = prefs[EMAIL_KEY] ?: return@map emptyList()
        val set = prefs[historyStepsKey(email)] ?: emptySet()
        set.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.sortedByDescending { it.first }
    }

    fun isLoggedInFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }

    fun savedEmailFlow(context: Context): Flow<String> = context.dataStore.data.map { it[EMAIL_KEY] ?: "" }

    // ==================== Operations ====================
    suspend fun saveGoal(context: Context, goal: Int) {
        val email = getCurrentEmail(context)
        if (email.isBlank()) return
        context.dataStore.edit { it[goalKey(email)] = goal.coerceIn(1000, 50000) }
    }

    suspend fun savePreviousDayToHistory(context: Context, previousDate: String, previousSteps: Int) {
        if (previousSteps <= 0 || previousDate.isBlank()) return
        val email = getCurrentEmail(context)
        if (email.isBlank()) return
        val entry = "$previousDate:$previousSteps"
        context.dataStore.edit {
            val key = historyStepsKey(email)
            val set = (it[key] ?: emptySet()).toMutableSet()
            if (entry !in set) set.add(entry)
            it[key] = set
        }
    }

    // Save credentials for a specific email (supports multi-account)
    suspend fun saveCredentials(context: Context, email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return
        context.dataStore.edit {
            it[passwordKey(trimmedEmail)] = password  // Per-email storage
            it[EMAIL_KEY] = trimmedEmail  // Set as current user
            it[IS_LOGGED_IN_KEY] = true
        }
    }

    // Check if email is already registered
    suspend fun isEmailRegistered(context: Context, email: String): Boolean {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return false
        val prefs = context.dataStore.data.first()
        return prefs[passwordKey(trimmedEmail)] != null
    }

    // ==================== Verification for Login (Per-Email) ====================
    suspend fun verifyCredentials(context: Context, email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) return false
        val prefs = context.dataStore.data.first()
        val savedPassword = prefs[passwordKey(trimmedEmail)] ?: ""
        return savedPassword == password
    }
}