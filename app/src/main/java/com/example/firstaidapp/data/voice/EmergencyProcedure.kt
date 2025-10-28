package com.example.firstaidapp.data.voice


/**
 * Voice preferences for the assistant
 */
data class VoicePreferences(
    val isEnabled: Boolean = true,
    val voiceSpeed: Float = 1.0f, // 0.5 to 2.0
    val voicePitch: Float = 1.0f, // 0.5 to 2.0
    val autoSpeak: Boolean = true,
    val hapticFeedback: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val language: String = "en-US",
    val offlineMode: Boolean = false
)

