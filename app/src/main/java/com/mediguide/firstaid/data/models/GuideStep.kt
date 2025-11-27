package com.mediguide.firstaid.data.models

// Model representing a single step within a first aid guide

import com.mediguide.firstaid.data.models.StepType
import com.google.gson.annotations.SerializedName

data class GuideStep(
    val id: String,
    @SerializedName("guideId")
    val guideId: String = "",
    val stepNumber: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description", alternate = ["instruction"])
    val description: String,
    val stepType: StepType? = StepType.ACTION,
    @SerializedName("isEmergencyStep", alternate = ["isCritical"])
    val isCritical: Boolean = false,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val duration: String? = null, // Duration as text (e.g., "10-15 seconds")
    val detailedInstructions: String? = null, // Detailed instructions
    val iconRes: Int? = null, // Icon resource ID
    val imageRes: Int? = null, // Image resource ID for step illustration
    val tips: List<String>? = null, // Helpful tips
    val warnings: List<String>? = null, // Warnings
    val requiredTools: List<String>? = null // Required tools/materials
)
