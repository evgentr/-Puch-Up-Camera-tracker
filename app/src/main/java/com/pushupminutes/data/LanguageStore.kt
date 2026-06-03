package com.pushupminutes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore("language_store")

class LanguageStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language_tag")

    val languageFlow: Flow<String> = context.languageDataStore.data.map { prefs ->
        prefs[languageKey] ?: "en"
    }

    suspend fun setLanguage(tag: String) {
        context.languageDataStore.edit { prefs ->
            prefs[languageKey] = tag
        }
    }
}
