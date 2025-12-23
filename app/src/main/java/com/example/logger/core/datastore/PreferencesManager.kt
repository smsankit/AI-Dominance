package com.example.logger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
    }

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
}
