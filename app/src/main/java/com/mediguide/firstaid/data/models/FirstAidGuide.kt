package com.mediguide.firstaid.data.models

// Model representing a first aid guide and its metadata

data class FirstAidGuide(
    val id: String,
    val title: String,
    val category: String,
    val severity: String,
    val description: String,
    val steps: List<GuideStep> = emptyList(),
    val iconResName: String? = null,
    val whenToCallEmergency: String? = null,
    val warnings: List<String>? = null,
    val estimatedTimeMinutes: Int = 0,
    val difficulty: String = "Beginner",
    val youtubeLink: String? = null,
    val isFavorite: Boolean = false,
    val lastAccessedTimestamp: Long = 0,
    val viewCount: Int = 0
)
