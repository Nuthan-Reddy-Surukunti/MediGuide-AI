package com.mediguide.firstaid.data.models

// User's medical info model used for emergency details

/**
 * Data class representing user's medical information
 * All data is stored locally for privacy
 */
data class MedicalInfo(
    val bloodType: String = "",
    val allergies: String = "",
    val medications: String = "",
    val medicalConditions: String = "",
    val emergencyNotes: String = "",
    val doctorName: String = "",
    val doctorContact: String = ""
)

