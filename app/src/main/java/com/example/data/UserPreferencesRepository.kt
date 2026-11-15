package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cma_prefs")

data class UserSettings(
  val darkModeEnabled: Boolean = true,
  val soundEnabled: Boolean = true,
  val hapticEnabled: Boolean = true,
  val bestStreak: Int = 0,
)

class UserPreferencesRepository(private val context: Context) {
  private object Keys {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val BEST_STREAK = intPreferencesKey("best_streak")
  }

  val settingsFlow: Flow<UserSettings> =
    context.dataStore.data.map { prefs ->
      UserSettings(
        darkModeEnabled = prefs[Keys.DARK_MODE] ?: true,
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
        bestStreak = prefs[Keys.BEST_STREAK] ?: 0,
      )
    }

  suspend fun setDarkMode(enabled: Boolean) {
    context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setHapticEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
  }

  suspend fun updateBestStreak(streak: Int) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.BEST_STREAK] ?: 0
      if (streak > current) prefs[Keys.BEST_STREAK] = streak
    }
  }
}
