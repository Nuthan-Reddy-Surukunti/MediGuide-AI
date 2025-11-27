package com.mediguide.firstaid.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mediguide.firstaid.data.models.MedicalInfo
import com.mediguide.firstaid.data.models.UserProfile
import com.mediguide.firstaid.utils.UserPreferencesManager
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel for Profile screen
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = UserPreferencesManager(application)

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _medicalInfo = MutableLiveData<MedicalInfo>()
    val medicalInfo: LiveData<MedicalInfo> = _medicalInfo

    init {
        loadUserProfile()
        loadMedicalInfo()
        // Auto-populate from Firebase if profile is empty
        syncFromFirebaseAuth()
    }

    /**
     * Load user profile from preferences
     */
    private fun loadUserProfile() {
        val profile = UserProfile(
            name = prefsManager.userName,
            bio = prefsManager.userBio,
            profileImageUri = prefsManager.profileImageUri,
            dateJoined = prefsManager.dateJoined
        )
        _userProfile.value = profile
    }

    /**
     * Load medical information from preferences
     */
    private fun loadMedicalInfo() {
        val medical = MedicalInfo(
            bloodType = prefsManager.bloodType,
            allergies = prefsManager.allergies,
            medications = prefsManager.medications,
            medicalConditions = prefsManager.medicalConditions,
            emergencyNotes = prefsManager.emergencyNotes,
            doctorName = prefsManager.doctorName,
            doctorContact = prefsManager.doctorContact
        )
        _medicalInfo.value = medical
    }

    /**
     * Save user profile
     */
    fun saveUserProfile(name: String, bio: String, imageUri: String = "") {
        prefsManager.userName = name
        prefsManager.userBio = bio
        if (imageUri.isNotEmpty()) {
            prefsManager.profileImageUri = imageUri
        }
        loadUserProfile()
    }

    /**
     * Save profile image
     */
    fun saveProfileImage(imageUri: String) {
        prefsManager.profileImageUri = imageUri
        loadUserProfile()
    }

    /**
     * Save medical information
     */
    fun saveMedicalInfo(
        bloodType: String,
        allergies: String,
        medications: String,
        medicalConditions: String,
        emergencyNotes: String,
        doctorName: String,
        doctorContact: String
    ) {
        prefsManager.bloodType = bloodType
        prefsManager.allergies = allergies
        prefsManager.medications = medications
        prefsManager.medicalConditions = medicalConditions
        prefsManager.emergencyNotes = emergencyNotes
        prefsManager.doctorName = doctorName
        prefsManager.doctorContact = doctorContact
        loadMedicalInfo()
    }

    /**
     * Get user initials for profile picture placeholder
     */
    fun getUserInitials(): String {
        val name = _userProfile.value?.name ?: return "?"
        if (name.isEmpty()) return "?"

        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            else -> parts[0].take(2).uppercase()
        }
    }

    /**
     * Sync user profile from Firebase Auth
     * This auto-populates name and photo when user logs in via Google
     */
    fun syncFromFirebaseAuth() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Only populate if name is empty (first time login)
            if (prefsManager.userName.isEmpty()) {
                currentUser.displayName?.let { displayName ->
                    if (displayName.isNotEmpty()) {
                        prefsManager.userName = displayName
                    }
                }
            }

            // Only populate photo if empty (first time login via Google)
            if (prefsManager.profileImageUri.isEmpty()) {
                currentUser.photoUrl?.let { photoUrl ->
                    prefsManager.profileImageUri = photoUrl.toString()
                }
            }

            // Reload profile after sync
            loadUserProfile()
        }
    }
}
