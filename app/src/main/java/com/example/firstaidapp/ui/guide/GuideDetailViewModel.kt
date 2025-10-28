package com.example.firstaidapp.ui.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.firstaidapp.data.models.FirstAidGuide
import com.example.firstaidapp.managers.JsonGuideManager
import kotlinx.coroutines.launch

class GuideDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val guideManager = JsonGuideManager(application)

    private val _guideId = MutableLiveData<String>()
    val guide: LiveData<FirstAidGuide?> = _guideId.switchMap { id ->
        guideManager.getGuideById(id)
    }

    fun loadGuide(guideId: String) {
        _guideId.value = guideId

        viewModelScope.launch {
            guideManager.updateLastAccessed(guideId)
        }
    }

    fun toggleFavorite(guideId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            guideManager.toggleFavorite(guideId, isFavorite)
        }
    }

    fun markStepCompleted(step: com.example.firstaidapp.data.models.GuideStep) {
        viewModelScope.launch {
            // Handle step completion logic
            // This could involve updating user progress, analytics, etc.
            // For now, we'll just log the completion
            android.util.Log.d("GuideDetailViewModel", "Step completed: ${step.title}")

            // You can add more logic here such as:
            // - Updating user progress in database
            // - Sending analytics events
            // - Triggering haptic feedback
        }
    }
}
