package com.example.firstaidapp.ui.home

// ViewModel for home screen: provides guides and categorization

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.firstaidapp.data.database.AppDatabase
import com.example.firstaidapp.data.models.FirstAidGuide
import com.example.firstaidapp.data.repository.GuideRepository
import com.example.firstaidapp.utils.DataInitializer
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Repository used to load guides from the DB
    private lateinit var repository: GuideRepository

    // LiveData exposing a flattened list of category headers and guide items
    private val _categorizedItems = MutableLiveData<List<CategoryItem>>()
    val categorizedItems: LiveData<List<CategoryItem>> = _categorizedItems

    // Tracks which categories are expanded in the UI
    private val expandedCategories = mutableSetOf<String>()

    // Cached list of guides loaded from the repository
    private val _guides = MutableLiveData<List<FirstAidGuide>>()

    // LiveData for search results when the user searches guides
    private val _searchResults = MutableLiveData<List<FirstAidGuide>>()
    val searchResults: LiveData<List<FirstAidGuide>> = _searchResults

    // Initialize repository and start data initialization/observation
    init {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(application)
            repository = GuideRepository(
                database.guideDao(),
                database.contactDao(),
                database.searchDao()
            )

            // Ensure static data is initialized on first run
            Log.d("HomeViewModel", "Starting data initialization")
            DataInitializer.initializeData(getApplication())

            // Observe all guides and update categorized items when data changes
            repository.allGuides.observeForever { guides ->
                Log.d("HomeViewModel", "Total guides available: ${guides.size}")
                _guides.value = guides
                updateCategorizedItems(guides)
            }
        }
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

    // Perform a repository search for guides matching the query
    fun searchGuides(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isNotEmpty()) {
                repository.searchGuidesList(query)
            } else {
                emptyList()
            }
        }
    }

    // Clear any active search results
    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}
