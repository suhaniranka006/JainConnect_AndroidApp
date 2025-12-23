package com.mycompany.jainconnect.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jain_saved_items", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_MONKS = "saved_monks"
        const val KEY_EVENTS = "saved_events"
        const val KEY_TITHIS = "saved_tithis"
        const val KEY_TEMPLES = "saved_temples"
        const val KEY_FOOD = "saved_food"
        const val KEY_POSTS = "saved_posts"
        const val KEY_NEWS = "saved_news"
        const val KEY_STORIES = "saved_stories"
    }

    fun isSaved(id: String, type: String): Boolean {
        val savedIds = getSavedIds(type)
        return savedIds.contains(id)
    }

    fun toggleSave(id: String, type: String): Boolean {
        val savedIds = getSavedIds(type).toMutableSet()
        val isNowSaved = if (savedIds.contains(id)) {
            savedIds.remove(id)
            false
        } else {
            savedIds.add(id)
            true
        }
        saveIds(type, savedIds)
        return isNowSaved
    }

    fun getSavedIds(type: String): Set<String> {
        val json = prefs.getString(type, null) ?: return emptySet()
        val typeToken = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, typeToken) ?: emptySet()
    }

    private fun saveIds(type: String, ids: Set<String>) {
        val json = gson.toJson(ids)
        prefs.edit().putString(type, json).apply()
    }
}
