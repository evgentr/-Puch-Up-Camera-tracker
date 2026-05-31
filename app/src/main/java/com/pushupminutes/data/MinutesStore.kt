package com.pushupminutes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.minutesDataStore by preferencesDataStore("minutes_store")

class MinutesStore(private val context: Context) {
    private val minutesKey = intPreferencesKey("minutes_balance")

    val minutesFlow: Flow<Int> = context.minutesDataStore.data.map { prefs ->
        prefs[minutesKey] ?: 0
    }

    suspend fun addMinutes(count: Int) {
        context.minutesDataStore.edit { prefs ->
            val current = prefs[minutesKey] ?: 0
            prefs[minutesKey] = current + count
        }
    }

    suspend fun spendMinute(): Boolean {
        var success = false
        context.minutesDataStore.edit { prefs ->
            val current = prefs[minutesKey] ?: 0
            if (current > 0) {
                prefs[minutesKey] = current - 1
                success = true
            }
        }
        return success
    }
}
