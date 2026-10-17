package com.homearcade.tv.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "home_arcade_tv_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_ACTIVE_PROFILE_ID = intPreferencesKey("active_profile_id")
        private val KEY_ACTIVE_PROFILE_NAME = stringPreferencesKey("active_profile_name")
        private val KEY_ACTIVE_PROFILE_COLOR = stringPreferencesKey("active_profile_color")
        private val KEY_PREFER_NATIVE_STREAM = booleanPreferencesKey("prefer_native_stream")
    }

    val serverUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVER_URL]
    }.distinctUntilChanged()

    val activeProfileId: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PROFILE_ID] ?: 1
    }.distinctUntilChanged()

    val activeProfileName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PROFILE_NAME] ?: "Player 1"
    }.distinctUntilChanged()

    val activeProfileColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PROFILE_COLOR] ?: "#EC4899"
    }.distinctUntilChanged()

    val preferNativeStream: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREFER_NATIVE_STREAM] ?: true
    }.distinctUntilChanged()

    suspend fun setServerUrl(url: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = cleanUrl
        }
    }

    suspend fun setActiveProfile(id: Int, name: String, color: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_PROFILE_ID] = id
            prefs[KEY_ACTIVE_PROFILE_NAME] = name
            prefs[KEY_ACTIVE_PROFILE_COLOR] = color
        }
    }

    suspend fun setPreferNativeStream(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREFER_NATIVE_STREAM] = enabled
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
