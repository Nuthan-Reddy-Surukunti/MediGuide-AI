package com.example.firstaidapp.data

// Simple data holder for user profile information
/**
 * Data class representing user profile information
 */
data class UserProfile(
    val name: String = "",
    val bio: String = "",
    val profileImageUri: String = "",
    val dateJoined: Long = System.currentTimeMillis()
)
