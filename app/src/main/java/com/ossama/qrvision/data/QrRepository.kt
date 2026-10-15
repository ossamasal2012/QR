package com.ossama.qrvision.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ossama.qrvision.domain.QrContentType
import com.ossama.qrvision.domain.QrClassifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.qrVisionDataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_vision_preferences")

class QrRepository(private val dataStore: DataStore<Preferences>) {
    private val historyKey = stringPreferencesKey("history_json")
    private val favoritesKey = stringPreferencesKey("favorites_json")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val vibrateKey = booleanPreferencesKey("vibrate_on_scan")
    private val soundKey = booleanPreferencesKey("sound_on_scan")
    private val autoOpenKey = booleanPreferencesKey("auto_open_result")

    val history: Flow<List<ScanEntry>> = dataStore.data
        .map { decodeEntries(it[historyKey].orEmpty()) }
        .distinctUntilChanged()

    val favorites: Flow<List<ScanEntry>> = dataStore.data
        .map { decodeEntries(it[favoritesKey].orEmpty()) }
        .distinctUntilChanged()

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[themeKey] ?: "system",
            vibrateOnScan = prefs[vibrateKey] ?: true,
            soundOnScan = prefs[soundKey] ?: false,
            autoOpenResult = prefs[autoOpenKey] ?: true
        )
    }.distinctUntilChanged()

    suspend fun addHistory(entry: ScanEntry) {
        dataStore.edit { prefs ->
            val current = decodeEntries(prefs[historyKey].orEmpty()).toMutableList()
            val duplicate = current.any { it.rawValue == entry.rawValue && kotlin.math.abs(it.timestamp - entry.timestamp) < 3_000L }
            if (!duplicate) {
                current.removeAll { it.rawValue == entry.rawValue && kotlin.math.abs(it.timestamp - entry.timestamp) < 15_000L }
                current.add(0, entry)
                prefs[historyKey] = encodeEntries(current.take(500))
            }
        }
    }

    suspend fun removeHistory(id: String) {
        dataStore.edit { prefs ->
            prefs[historyKey] = encodeEntries(decodeEntries(prefs[historyKey].orEmpty()).filterNot { it.id == id })
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { prefs -> prefs[historyKey] = "[]" }
    }

    suspend fun toggleFavorite(entry: ScanEntry) {
        dataStore.edit { prefs ->
            val current = decodeEntries(prefs[favoritesKey].orEmpty()).toMutableList()
            val alreadySaved = current.any { it.rawValue == entry.rawValue }
            if (alreadySaved) current.removeAll { it.rawValue == entry.rawValue }
            else current.add(0, entry)
            prefs[favoritesKey] = encodeEntries(current.take(500))
        }
    }

    suspend fun removeFavorite(id: String) {
        dataStore.edit { prefs ->
            prefs[favoritesKey] = encodeEntries(decodeEntries(prefs[favoritesKey].orEmpty()).filterNot { it.id == id })
        }
    }

    suspend fun clearFavorites() {
        dataStore.edit { prefs -> prefs[favoritesKey] = "[]" }
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[themeKey] = settings.themeMode
            prefs[vibrateKey] = settings.vibrateOnScan
            prefs[soundKey] = settings.soundOnScan
            prefs[autoOpenKey] = settings.autoOpenResult
        }
    }

    private fun encodeEntries(entries: List<ScanEntry>): String = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("id", entry.id)
                put("rawValue", entry.rawValue)
                put("type", entry.type.key)
                put("timestamp", entry.timestamp)
            })
        }
    }.toString()

    private fun decodeEntries(encoded: String): List<ScanEntry> {
        if (encoded.isBlank()) return emptyList()
        return try {
            val array = JSONArray(encoded)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val raw = obj.optString("rawValue", "")
                    if (raw.isEmpty()) continue
                    val typeKey = obj.optString("type", "")
                    add(
                        ScanEntry(
                            id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                            rawValue = raw,
                            type = if (typeKey.isBlank()) QrClassifier.classify(raw) else QrContentType.fromKey(typeKey),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
