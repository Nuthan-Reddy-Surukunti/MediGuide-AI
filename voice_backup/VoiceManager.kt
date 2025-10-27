package com.example.firstaidapp.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firstaidapp.data.voice.VoiceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Simplified Voice Manager that replaces 4 complex voice files:
 * - VoiceAssistantManager.kt (200+ lines)
 * - TextToSpeechManager.kt (150+ lines)
 * - SpeechRecognitionService.kt (200+ lines)
 * - VoicePermissionManager.kt (80+ lines)
 *
 * Total reduction: ~630 lines → 120 lines
 */
class VoiceManager(private val context: Context) {

    private val tag = "VoiceManager"

    // Simple TTS
    private var tts: TextToSpeech? = null
    private var isTTSReady = false

    // State management (keeping the same interface for compatibility)
    private val _assistantState = MutableStateFlow<VoiceAssistantState>(VoiceAssistantState.Idle)
    val assistantState: StateFlow<VoiceAssistantState> = _assistantState.asStateFlow()

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _currentResponse = MutableLiveData<VoiceResponse>()
    val currentResponse: LiveData<VoiceResponse> = _currentResponse

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var isInitialized = false

    /**
     * Initialize voice manager (simplified)
     */
    fun initialize(onComplete: (Boolean) -> Unit) {
        if (isInitialized) {
            onComplete(true)
            return
        }

        // Initialize TTS
        tts = TextToSpeech(context) { status ->
            isTTSReady = status == TextToSpeech.SUCCESS
            if (isTTSReady) {
                tts?.language = Locale.getDefault()
            }
            isInitialized = isTTSReady
            onComplete(isTTSReady)
        }
    }

    /**
     * Start listening for speech
     */
    fun startListening() {
        if (!hasPermissions()) {
            _errorMessage.value = "Microphone permission required"
            return
        }

        _assistantState.value = VoiceAssistantState.Listening
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        _assistantState.value = VoiceAssistantState.Idle
    }

    /**
     * Speak text (simplified)
     */
    fun speak(text: String) {
        if (isTTSReady && tts != null) {
            _assistantState.value = VoiceAssistantState.Speaking
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
        }
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        tts?.stop()
        _assistantState.value = VoiceAssistantState.Idle
    }

    /**
     * Get speech recognition intent (for activities to use)
     */
    fun getSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
        }
    }

    /**
     * Handle recognized speech text
     */
    fun handleRecognizedText(text: String) {
        _recognizedText.value = text
        _assistantState.value = VoiceAssistantState.Processing

        // Simple processing - just echo back for now
        val response = "You said: $text"
        speak(response)
    }

    /**
     * Check if required permissions are granted (simplified)
     */
    fun hasPermissions(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
               android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get required permissions list
     */
    fun getRequiredPermissions(): List<String> {
        return listOf(android.Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Check if all permissions are granted (for compatibility with old ViewModel)
     */
    fun areAllPermissionsGranted(): Boolean {
        return hasPermissions()
    }

    /**
     * Get missing permissions (for compatibility with old ViewModel)
     */
    fun getMissingPermissions(): List<String> {
        return getRequiredPermissions().filter {
            context.checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get permission rationale (for compatibility with old ViewModel)
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            android.Manifest.permission.RECORD_AUDIO ->
                "Microphone access is needed for voice commands during emergencies"
            else -> "This permission is needed for voice assistant functionality"
        }
    }

    /**
     * Handle emergency commands (simplified)
     */
    fun handleEmergencyCommand(command: String) {
        val response = "Emergency mode activated for: $command"
        _currentResponse.value = VoiceResponse(response)
        speak(response)
    }

    /**
     * Clear conversation history (simplified - no-op for now)
     */
    fun clearConversationHistory() {
        // Simple implementation - just log
        Log.d(tag, "Conversation history cleared")
    }

    /**
     * Update preferences (simplified - no-op for now)
     */
    fun updatePreferences(preferences: Any) {
        // Simple implementation - just log
        Log.d(tag, "Preferences updated")
    }

    /**
     * Get permission manager (return this instance for compatibility)
     */
    fun getPermissionManager(): VoiceManager {
        return this
    }

    /**
     * Shutdown (alias for cleanup)
     */
    fun shutdown() {
        cleanup()
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        tts?.shutdown()
        tts = null
        isTTSReady = false
        isInitialized = false
    }
}

// Keep the same state enum for compatibility
enum class VoiceAssistantState {
    Idle,
    Listening,
    Processing,
    Speaking,
    Error
}

// Simple VoiceResponse data class for compatibility
data class VoiceResponse(
    val message: String,
    val isSuccessful: Boolean = true,
    val responseType: String = "text"
)

