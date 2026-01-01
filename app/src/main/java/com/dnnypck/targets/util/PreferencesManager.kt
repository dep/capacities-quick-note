package com.dnnypck.capacitiesquicknote.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "capacities_preferences",
        Context.MODE_PRIVATE
    )

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun saveSpaceId(spaceId: String) {
        prefs.edit().putString(KEY_SPACE_ID, spaceId).apply()
    }

    fun getSpaceId(): String {
        return prefs.getString(KEY_SPACE_ID, "") ?: ""
    }

    fun hasRequiredSettings(): Boolean {
        return getApiKey().isNotBlank() && getSpaceId().isNotBlank()
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SPACE_ID = "space_id"
    }
}
