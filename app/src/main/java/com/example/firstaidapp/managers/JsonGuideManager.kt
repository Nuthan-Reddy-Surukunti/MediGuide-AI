package com.example.firstaidapp.managers

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firstaidapp.data.models.FirstAidGuide
import com.example.firstaidapp.data.repository.FirstAidGuidesData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages first aid guides from JSON file in assets.
 * Loads guides once and keeps them in memory for fast access.
 * Falls back to Kotlin data source if JSON is not available.
 */
class JsonGuideManager(private val context: Context) {

    private val gson = Gson()
    private var allGuides: List<FirstAidGuide> = emptyList()
    private val preferencesManager = PreferencesManager(context)

    // LiveData for observing guides
    private val _guidesLiveData = MutableLiveData<List<FirstAidGuide>>()
    private val _categoriesLiveData = MutableLiveData<List<String>>()
    private val _favoritesLiveData = MutableLiveData<List<FirstAidGuide>>()

    init {
        loadGuides()
    }

    /**
     * Load guides from JSON file in assets folder, or fallback to Kotlin data
     */
    private fun loadGuides() {
        try {
            // Try loading from JSON first
            val jsonString = context.assets.open("guides.json")
                .bufferedReader()
                .use { it.readText() }
                .trim()

            // Check if JSON is empty or just an empty array
            if (jsonString.isNotBlank() && jsonString != "[]" && jsonString.length > 10) {
                val type = object : TypeToken<List<FirstAidGuide>>() {}.type
                val guidesFromJson: List<FirstAidGuide>? = gson.fromJson(jsonString, type)

                if (guidesFromJson != null && guidesFromJson.isNotEmpty()) {
                    allGuides = guidesFromJson
                    android.util.Log.d("JsonGuideManager", "Loaded ${allGuides.size} guides from JSON")
                } else {
                    // Fallback to Kotlin data source
                    allGuides = FirstAidGuidesData.getAllFirstAidGuides()
                    android.util.Log.d("JsonGuideManager", "JSON empty, loaded ${allGuides.size} guides from Kotlin data")
                }
            } else {
                // Fallback to Kotlin data source
                allGuides = FirstAidGuidesData.getAllFirstAidGuides()
                android.util.Log.d("JsonGuideManager", "JSON invalid/empty, loaded ${allGuides.size} guides from Kotlin data")
            }
        } catch (e: Exception) {
            android.util.Log.e("JsonGuideManager", "Error loading guides from JSON, using Kotlin fallback", e)
            // Fallback to Kotlin data source
            allGuides = FirstAidGuidesData.getAllFirstAidGuides()
        }

        // Update LiveData
        _guidesLiveData.postValue(allGuides)
        updateCategoriesLiveData()
        updateFavoritesLiveData()

        android.util.Log.d("JsonGuideManager", "Final guides count: ${allGuides.size}")
    }

    // ==================== Get All Guides ====================

    fun getAllGuides(): LiveData<List<FirstAidGuide>> {
        return _guidesLiveData
    }

    fun getAllGuidesList(): List<FirstAidGuide> {
        return allGuides
    }

    // ==================== Get Guide by ID ====================

    fun getGuideById(guideId: String): LiveData<FirstAidGuide?> {
        val result = MutableLiveData<FirstAidGuide?>()
        result.value = allGuides.find { it.id == guideId }
        return result
    }

    fun getGuideByIdSync(guideId: String): FirstAidGuide? {
        return allGuides.find { it.id == guideId }
    }

    // ==================== Search Guides ====================

    fun searchGuides(query: String): LiveData<List<FirstAidGuide>> {
        val result = MutableLiveData<List<FirstAidGuide>>()

        if (query.isBlank()) {
            result.value = allGuides
        } else {
            val searchResults = allGuides.filter { guide ->
                guide.title.contains(query, ignoreCase = true) ||
                guide.description.contains(query, ignoreCase = true) ||
                guide.category.contains(query, ignoreCase = true) ||
                guide.severity.contains(query, ignoreCase = true) ||
                guide.steps.any { step ->
                    step.title.contains(query, ignoreCase = true) ||
                    step.description.contains(query, ignoreCase = true)
                }
            }
            result.value = searchResults
        }

        return result
    }

    fun searchGuidesList(query: String): List<FirstAidGuide> {
        if (query.isBlank()) return allGuides

        return allGuides.filter { guide ->
            guide.title.contains(query, ignoreCase = true) ||
            guide.description.contains(query, ignoreCase = true) ||
            guide.category.contains(query, ignoreCase = true)
        }
    }

    // ==================== Get by Category ====================

    fun getGuidesByCategory(category: String): LiveData<List<FirstAidGuide>> {
        val result = MutableLiveData<List<FirstAidGuide>>()
        result.value = allGuides.filter { it.category == category }
        return result
    }

    fun getGuidesByCategorySync(category: String): List<FirstAidGuide> {
        return allGuides.filter { it.category == category }
    }

    // ==================== Get Categories ====================

    fun getAllCategories(): LiveData<List<String>> {
        return _categoriesLiveData
    }

    fun getAllCategoriesList(): List<String> {
        return allGuides.map { it.category }.distinct().sorted()
    }

    private fun updateCategoriesLiveData() {
        _categoriesLiveData.postValue(getAllCategoriesList())
    }

    // ==================== Favorites ====================

    fun getFavoriteGuides(): LiveData<List<FirstAidGuide>> {
        return _favoritesLiveData
    }

    fun getFavoriteGuidesList(): List<FirstAidGuide> {
        val favoriteIds = preferencesManager.getFavorites()
        return allGuides
            .filter { favoriteIds.contains(it.id) }
            .sortedByDescending { preferencesManager.getLastAccessed(it.id) }
    }

    fun toggleFavorite(guideId: String, isFavorite: Boolean) {
        if (isFavorite) {
            preferencesManager.addFavorite(guideId)
        } else {
            preferencesManager.removeFavorite(guideId)
        }
        updateFavoritesLiveData()
    }

    fun isFavorite(guideId: String): Boolean {
        return preferencesManager.isFavorite(guideId)
    }

    private fun updateFavoritesLiveData() {
        _favoritesLiveData.postValue(getFavoriteGuidesList())
    }

    // ==================== View Count & Last Accessed ====================

    fun updateLastAccessed(guideId: String) {
        preferencesManager.updateLastAccessed(guideId)
        preferencesManager.incrementViewCount(guideId)
        updateFavoritesLiveData() // Update in case sorting changes
    }

    fun getViewCount(guideId: String): Int {
        return preferencesManager.getViewCount(guideId)
    }

    fun getLastAccessed(guideId: String): Long {
        return preferencesManager.getLastAccessed(guideId)
    }

    // ==================== Get Guide Count ====================

    fun getGuidesCount(): Int {
        return allGuides.size
    }

    // ==================== Enrich Guides with User Data ====================

    /**
     * Returns guide with user-specific data (favorite status, view count, etc.)
     */
    fun getEnrichedGuide(guideId: String): FirstAidGuide? {
        val guide = allGuides.find { it.id == guideId } ?: return null
        return guide.copy(
            isFavorite = preferencesManager.isFavorite(guideId),
            viewCount = preferencesManager.getViewCount(guideId),
            lastAccessedTimestamp = preferencesManager.getLastAccessed(guideId)
        )
    }

    /**
     * Returns all guides enriched with user data
     */
    fun getEnrichedGuides(): List<FirstAidGuide> {
        return allGuides.map { guide ->
            guide.copy(
                isFavorite = preferencesManager.isFavorite(guide.id),
                viewCount = preferencesManager.getViewCount(guide.id),
                lastAccessedTimestamp = preferencesManager.getLastAccessed(guide.id)
            )
        }
    }
}

