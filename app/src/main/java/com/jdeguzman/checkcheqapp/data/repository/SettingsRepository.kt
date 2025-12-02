package com.jdeguzman.checkcheqapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserSettings(
    val nearMeRadiusMeters: Int = 1000,   // 1 km
    val startWithNearMe: Boolean = false,
    val defaultCategory: String = "All"
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_NEAR_ME_RADIUS = intPreferencesKey("near_me_radius_meters")
        private val KEY_START_WITH_NEAR_ME = booleanPreferencesKey("start_with_near_me")
        private val KEY_DEFAULT_CATEGORY = stringPreferencesKey("default_category")
    }

    val settingsFlow: Flow<UserSettings> =
        dataStore.data.map { prefs ->
            UserSettings(
                nearMeRadiusMeters = prefs[KEY_NEAR_ME_RADIUS] ?: 1000,
                startWithNearMe = prefs[KEY_START_WITH_NEAR_ME] ?: false,
                defaultCategory = prefs[KEY_DEFAULT_CATEGORY] ?: "All"
            )
        }

    suspend fun setNearMeRadius(meters: Int) {
        dataStore.edit { it[KEY_NEAR_ME_RADIUS] = meters }
    }

    suspend fun setStartWithNearMe(enabled: Boolean) {
        dataStore.edit { it[KEY_START_WITH_NEAR_ME] = enabled }
    }

    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { it[KEY_DEFAULT_CATEGORY] = category }
    }
}
