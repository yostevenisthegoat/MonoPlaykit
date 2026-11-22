package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mono_playkit_prefs")

data class PlayKitSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val cubeInspectionEnabled: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val saveDiceHistory: Boolean = true,
    val defaultScoreboardPreset: String = "CUSTOM",
    val defaultRedTeamName: String = "Red Team",
    val defaultBlueTeamName: String = "Blue Team",
    val recentSearches: List<String> = emptyList(),
)

class PlayKitPreferences(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val CUBE_INSPECTION = booleanPreferencesKey("cube_inspection")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val SAVE_DICE_HISTORY = booleanPreferencesKey("save_dice_history")
        val DEFAULT_SCOREBOARD_PRESET = stringPreferencesKey("default_scoreboard_preset")
        val DEFAULT_RED_NAME = stringPreferencesKey("default_red_name")
        val DEFAULT_BLUE_NAME = stringPreferencesKey("default_blue_name")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }

    val settings: Flow<PlayKitSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val theme = ThemeMode.entries.find { it.name == themeStr } ?: ThemeMode.DARK
        val searchesRaw = prefs[Keys.RECENT_SEARCHES].orEmpty()
        val searches = if (searchesRaw.isBlank()) emptyList() else searchesRaw.split("|").filter { it.isNotBlank() }

        PlayKitSettings(
            themeMode = theme,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            cubeInspectionEnabled = prefs[Keys.CUBE_INSPECTION] ?: true,
            keepScreenAwake = prefs[Keys.KEEP_SCREEN_AWAKE] ?: true,
            saveDiceHistory = prefs[Keys.SAVE_DICE_HISTORY] ?: true,
            defaultScoreboardPreset = prefs[Keys.DEFAULT_SCOREBOARD_PRESET] ?: "CUSTOM",
            defaultRedTeamName = prefs[Keys.DEFAULT_RED_NAME] ?: "Red Team",
            defaultBlueTeamName = prefs[Keys.DEFAULT_BLUE_NAME] ?: "Blue Team",
            recentSearches = searches,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setCubeInspectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CUBE_INSPECTION] = enabled }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun setSaveDiceHistory(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAVE_DICE_HISTORY] = enabled }
    }

    suspend fun setDefaultScoreboardPreset(presetKey: String) {
        context.dataStore.edit { it[Keys.DEFAULT_SCOREBOARD_PRESET] = presetKey }
    }

    suspend fun setDefaultTeamNames(red: String, blue: String) {
        context.dataStore.edit {
            it[Keys.DEFAULT_RED_NAME] = red.ifBlank { "Red Team" }
            it[Keys.DEFAULT_BLUE_NAME] = blue.ifBlank { "Blue Team" }
        }
    }

    suspend fun addRecentSearch(query: String) {
        val clean = query.trim().replace("|", "")
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]
                ?.split("|")
                ?.filter { it.isNotBlank() }
                .orEmpty()
            val updated = (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) }).take(8)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("|")
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { it.remove(Keys.RECENT_SEARCHES) }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
