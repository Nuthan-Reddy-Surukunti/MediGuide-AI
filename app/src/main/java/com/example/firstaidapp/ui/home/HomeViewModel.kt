package com.example.firstaidapp.ui.home

// ViewModel for home screen: provides guides and categorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.firstaidapp.data.models.FirstAidGuide
import com.example.firstaidapp.managers.JsonGuideManager
import com.example.firstaidapp.managers.PreferencesManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Manager used to load guides from JSON/Kotlin data
    private val guideManager = JsonGuideManager(application)
    private val preferencesManager = PreferencesManager(application)

    // LiveData exposing a flattened list of category headers and guide items
    private val _categorizedItems = MutableLiveData<List<CategoryItem>>()
    val categorizedItems: LiveData<List<CategoryItem>> = _categorizedItems

    // Tracks which categories are expanded in the UI
    private val expandedCategories = mutableSetOf<String>()

    // Cached list of guides loaded from the manager
    private val _guides = MutableLiveData<List<FirstAidGuide>>()

    // LiveData for all guides (for external observers)
    private val _guidesLiveData = MutableLiveData<List<FirstAidGuide>>()
    val guidesLiveData: LiveData<List<FirstAidGuide>> = _guidesLiveData

    // LiveData for search results when the user searches guides
    private val _searchResults = MutableLiveData<List<FirstAidGuide>>()
    val searchResults: LiveData<List<FirstAidGuide>> = _searchResults

    // Initialize manager and start data observation
    init {
        // Load all guides directly from the manager
        val guides = guideManager.getAllGuidesList()
        Log.d("HomeViewModel", "Total guides available: ${guides.size}")
        _guides.value = guides
        updateCategorizedItems(guides)

        // Also update LiveData for observers
        _guidesLiveData.value = guides
    }

    // Build the list of CategoryItem (headers + optional guide items) for the UI
    private fun updateCategorizedItems(guides: List<FirstAidGuide>) {
        val categorizedGuides = GuideCategories.getCategorizedGuides(guides)
        val items = mutableListOf<CategoryItem>()

        categorizedGuides.forEach { category ->
            // Add a category header item
            items.add(
                CategoryItem.CategoryHeader(
                    title = category.title,
                    icon = category.icon,
                    description = category.description,
                    isExpanded = expandedCategories.contains(category.title),
                    guideCount = category.guides.size
                )
            )

            // If expanded, add guide items under the header
            if (expandedCategories.contains(category.title)) {
                items.addAll(category.guides.map { CategoryItem.GuideItem(it) })
            }
        }

        // Publish the flattened list to observers
        _categorizedItems.value = items
    }

    // Toggle the expanded/collapsed state for a category and refresh items
    fun toggleCategory(categoryTitle: String) {
        if (expandedCategories.contains(categoryTitle)) {
            expandedCategories.remove(categoryTitle)
        } else {
            expandedCategories.add(categoryTitle)
        }
        _guides.value?.let { updateCategorizedItems(it) }
    }

    // Perform a search for guides matching the query
    fun searchGuides(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isNotEmpty()) {
                guideManager.searchGuidesList(query).also {
                    preferencesManager.addSearchQuery(query)
                }
            } else {
                emptyList()
            }
        }
    }

    // Search guides by title (for navigation from quick action cards)
    fun searchGuidesByTitle(title: String): List<FirstAidGuide> {
        return guideManager.searchGuidesList(title)
    }

    // Clear any active search results
    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}
