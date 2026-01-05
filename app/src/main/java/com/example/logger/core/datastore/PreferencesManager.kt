package com.example.logger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.logger.domain.model.TeamMemberData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val LAST_OPENED = longPreferencesKey("last_opened")
        val TEAM_MEMBERS = stringPreferencesKey("team_members")
    }

    private val gson = Gson()

    // Generic get by key
    fun <T> get(key: Preferences.Key<T>): Flow<T?> = dataStore.data.map { prefs ->
        prefs[key]
    }

    // Generic put by key
    suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    // Generic remove by key
    suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    // Clear all preferences
    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Example specific usage maintained
    val lastOpened: Flow<Long?> = get(Keys.LAST_OPENED)

    suspend fun setLastOpened(epochMillis: Long) {
        put(Keys.LAST_OPENED, epochMillis)
    }

    // Store a list of team members as JSON
    suspend fun saveTeamMembers(members: List<TeamMemberData>) {
        val json = gson.toJson(members)
        put(Keys.TEAM_MEMBERS, json)
    }

    // Retrieve a list of team members from JSON
    fun getTeamMembers(): Flow<List<TeamMemberData>> = get(Keys.TEAM_MEMBERS).map { json ->
        if (json.isNullOrEmpty()) emptyList() else gson.fromJson(json, object : TypeToken<List<TeamMemberData>>() {}.type)
    }
}
