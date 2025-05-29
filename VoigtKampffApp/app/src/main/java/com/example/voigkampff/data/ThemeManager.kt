package com.example.voigkampff.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define a Preferences DataStore instance at the top level
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeManager(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    // Flow to observe the dark mode preference
    val isDarkMode: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false // Default to light mode (false)
        }

    // Function to update the dark mode preference
    suspend fun setDarkMode(isDark: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}