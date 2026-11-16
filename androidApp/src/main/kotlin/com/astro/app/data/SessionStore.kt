package com.astro.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.astro.app.core.AppConstants
import com.astro.app.core.LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = AppConstants.DATASTORE_NAME)

// Offline-first local store: session history (with outbox flag) and manual inputs.
class SessionStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val sessionsKey = stringPreferencesKey("sessions")
    private val manualKey = stringPreferencesKey("manual")

    // Chronological, oldest first.
    val sessions: Flow<List<SessionRecord>> = context.dataStore.data.map { prefs ->
        prefs[sessionsKey]?.let { decode<List<SessionRecord>>(it) }.orEmpty()
    }

    val manual: Flow<ManualEntry> = context.dataStore.data.map { prefs ->
        prefs[manualKey]?.let { decode<ManualEntry>(it) } ?: ManualEntry()
    }

    suspend fun append(record: SessionRecord) {
        context.dataStore.edit { prefs ->
            val current = prefs[sessionsKey]?.let { decode<List<SessionRecord>>(it) }.orEmpty()
            prefs[sessionsKey] = json.encodeToString(current + record)
        }
    }

    suspend fun saveManual(entry: ManualEntry) {
        context.dataStore.edit { it[manualKey] = json.encodeToString(entry) }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    private inline fun <reified T> decode(raw: String): T? = try {
        json.decodeFromString<T>(raw)
    } catch (e: SerializationException) {
        LOG.e("Corrupted local store entry", e)
        null
    }
}
