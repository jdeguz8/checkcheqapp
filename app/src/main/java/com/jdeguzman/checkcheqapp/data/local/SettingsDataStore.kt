package com.jdeguzman.checkcheqapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension property to access the app-wide [DataStore] for settings.
 *
 * Backed by a `settings.preferences_pb` proto file under app storage.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("settings")
