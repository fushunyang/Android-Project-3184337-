package com.example.tree.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tree_prefs")

object UserPreferences {
    // Key 定义
    val DAILY_STEPS_KEY = intPreferencesKey("daily_steps")
    val LAST_TOTAL_STEPS_KEY = longPreferencesKey("last_total_steps")
    val LAST_DATE_KEY = stringPreferencesKey("last_date")
    val GOAL_KEY = intPreferencesKey("daily_goal")
    val HISTORY_STEPS_KEY = stringSetPreferencesKey("history_steps")  // 历史记录（Set 自动去重）

    // 每日步数 Flow（实时）
    fun dailyStepsFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_STEPS_KEY] ?: 0
    }

    // 目标步数 Flow
    fun goalFlow(context: Context): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GOAL_KEY] ?: 10000
    }

    // 历史记录 Flow（返回 List<Pair<日期, 步数>>，已按日期倒序）
    fun historyStepsFlow(context: Context): Flow<List<Pair<String, Int>>> = context.dataStore.data.map { prefs ->
        val set = prefs[HISTORY_STEPS_KEY] ?: emptySet()
        set.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) {
                parts[0] to (parts[1].toIntOrNull() ?: 0)
            } else null
        }.sortedByDescending { it.first }
    }

    // 保存目标
    suspend fun saveGoal(context: Context, goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[GOAL_KEY] = goal.coerceIn(1000, 50000)
        }
    }

    // ★★★★★ 关键新增：自动把「昨天」的步数保存到历史记录（跨天时调用）★★★★★
    suspend fun savePreviousDayIfNeeded(
        context: Context,
        previousDate: String,
        previousDaySteps: Int
    ) {
        // 只有昨天有步数才保存（避免保存 0）
        if (previousDaySteps <= 0 || previousDate.isBlank()) return

        val entry = "$previousDate:$previousDaySteps"

        context.dataStore.edit { prefs ->
            val currentSet = (prefs[HISTORY_STEPS_KEY] ?: emptySet()).toMutableSet()
            if (entry !in currentSet) {
                currentSet.add(entry)
                prefs[HISTORY_STEPS_KEY] = currentSet
            }
        }
    }
}