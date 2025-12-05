package com.jdeguzman.checkcheqapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jdeguzman.checkcheqapp.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Strongly-typed snapshot of all persisted settings.
 */
data class SettingsData(
    val nearMeRadiusMeters: Int = 1000,
    val startWithNearMe: Boolean = false,
    val defaultCategory: String = "All",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/**
 * Repository that wraps [DataStore] access for user settings.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val NEAR_ME_RADIUS = intPreferencesKey("near_me_radius_m")
        val START_WITH_NEAR_ME = booleanPreferencesKey("start_with_near_me")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /**
     * Observe all persisted settings as a single stream.
     */
    val settingsFlow: Flow<SettingsData> = dataStore.data.map { prefs ->
        val radius = prefs[Keys.NEAR_ME_RADIUS] ?: 1000
        val near = prefs[Keys.START_WITH_NEAR_ME] ?: false
        val category = prefs[Keys.DEFAULT_CATEGORY] ?: "All"
        val themeStr = prefs[Keys.THEME_MODE] ?: "system"

        val themeMode = when (themeStr) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }

        SettingsData(
            nearMeRadiusMeters = radius,
            startWithNearMe = near,
            defaultCategory = category,
            themeMode = themeMode
        )
    }

    /**
     * Persist a new near-me radius.
     *
     * @param meters radius in meters.
     */
    suspend fun setNearMeRadius(meters: Int) {
        dataStore.edit { it[Keys.NEAR_ME_RADIUS] = meters }
    }

    /**
     * Persist whether the feed should start with near-me filtering enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    suspend fun setStartWithNearMe(enabled: Boolean) {
        dataStore.edit { it[Keys.START_WITH_NEAR_ME] = enabled }
    }

    /**
     * Persist the default category filter used by the feed.
     *
     * @param category category name (e.g. "Grocery", "All").
     */
    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { it[Keys.DEFAULT_CATEGORY] = category }
    }

    /**
     * Persist the current theme selection.
     *
     * @param mode one of [ThemeMode.SYSTEM], [ThemeMode.LIGHT], or [ThemeMode.DARK].
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        val encoded = when (mode) {
            ThemeMode.SYSTEM -> "system"
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
        }
        dataStore.edit { it[Keys.THEME_MODE] = encoded }
    }
}
