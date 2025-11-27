package com.mediguide.firstaid.managers

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mediguide.firstaid.data.models.EmergencyContact

/**
 * Manages user preferences using SharedPreferences.
 * Handles favorites, view counts, search history, and user-added contacts.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("first_aid_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==================== Favorites ====================

    fun addFavorite(guideId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(guideId)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun removeFavorite(guideId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(guideId)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(guideId: String): Boolean {
        return getFavorites().contains(guideId)
    }

    fun toggleFavorite(guideId: String): Boolean {
        return if (isFavorite(guideId)) {
            removeFavorite(guideId)
            false
        } else {
            addFavorite(guideId)
            true
        }
    }

    // ==================== View Count ====================

    fun incrementViewCount(guideId: String) {
        val currentCount = getViewCount(guideId)
        prefs.edit().putInt("$KEY_VIEW_COUNT_PREFIX$guideId", currentCount + 1).apply()
    }

    fun getViewCount(guideId: String): Int {
        return prefs.getInt("$KEY_VIEW_COUNT_PREFIX$guideId", 0)
    }

    // ==================== Last Accessed ====================

    fun updateLastAccessed(guideId: String) {
        val timestamp = System.currentTimeMillis()
        prefs.edit().putLong("$KEY_LAST_ACCESSED_PREFIX$guideId", timestamp).apply()
    }

    fun getLastAccessed(guideId: String): Long {
        return prefs.getLong("$KEY_LAST_ACCESSED_PREFIX$guideId", 0)
    }

    // ==================== Search History ====================

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return

        val history = getSearchHistory().toMutableList()
        history.remove(query) // Remove if already exists
        history.add(0, query) // Add to top

        // Keep only last 10 searches
        while (history.size > 10) {
            history.removeAt(history.lastIndex)
        }

        val json = gson.toJson(history)
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply()
    }

    fun getSearchHistory(): List<String> {
        val json = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    fun getRecentSearches(limit: Int = 5): List<String> {
        return getSearchHistory().take(limit)
    }

    // ==================== User Contacts ====================

    fun addUserContact(contact: EmergencyContact) {
        val contacts = getUserContacts().toMutableList()
        contacts.add(contact)
        saveUserContacts(contacts)
    }

    fun updateUserContact(contact: EmergencyContact) {
        val contacts = getUserContacts().toMutableList()
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            contacts[index] = contact
            saveUserContacts(contacts)
        }
    }

    fun deleteUserContact(contactId: Long) {
        val contacts = getUserContacts().toMutableList()
        contacts.removeAll { it.id == contactId }
        saveUserContacts(contacts)
    }

    fun getUserContacts(): List<EmergencyContact> {
        val json = prefs.getString(KEY_USER_CONTACTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<EmergencyContact>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUserContacts(contacts: List<EmergencyContact>) {
        val json = gson.toJson(contacts)
        prefs.edit().putString(KEY_USER_CONTACTS, json).apply()
    }

    fun getNextContactId(): Long {
        val currentId = prefs.getLong(KEY_NEXT_CONTACT_ID, 1000L)
        prefs.edit().putLong(KEY_NEXT_CONTACT_ID, currentId + 1).apply()
        return currentId
    }

    // ==================== Data Initialization Flag ====================

    fun isDataInitialized(): Boolean {
        return prefs.getBoolean(KEY_DATA_INITIALIZED, false)
    }

    fun setDataInitialized(initialized: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_INITIALIZED, initialized).apply()
    }

    // ==================== Clear All Data ====================

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_VIEW_COUNT_PREFIX = "view_count_"
        private const val KEY_LAST_ACCESSED_PREFIX = "last_accessed_"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_USER_CONTACTS = "user_contacts"
        private const val KEY_NEXT_CONTACT_ID = "next_contact_id"
        private const val KEY_DATA_INITIALIZED = "data_initialized"
    }
}

