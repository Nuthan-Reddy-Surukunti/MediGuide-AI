package com.mediguide.firstaid.ui.voice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mediguide.firstaid.data.voice.VoiceCommandType
import com.mediguide.firstaid.data.voice.VoicePreferences
import com.mediguide.firstaid.data.voice.VoiceResponse
import com.mediguide.firstaid.voice.VoiceAssistantManager
import com.mediguide.firstaid.voice.VoiceAssistantState
import com.mediguide.firstaid.voice.VoicePermissionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Voice Assistant UI
 */
class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private var voiceAssistant: VoiceAssistantManager? = null

    // Create fallback LiveData/StateFlow for when voice assistant isn't available
    private val _assistantState = MutableLiveData<VoiceAssistantState>(VoiceAssistantState.Idle)
    private val _currentResponse = MutableLiveData<VoiceResponse>()
    private val _recognizedText = MutableLiveData<String>()
    private val _errorMessage = MutableLiveData<String>()

    // Expose state from voice assistant (with fallbacks)
    val assistantState: LiveData<VoiceAssistantState> = _assistantState
    val currentResponse: LiveData<VoiceResponse> = _currentResponse
    val recognizedText: LiveData<String> = _recognizedText
    val errorMessage: LiveData<String> = _errorMessage

    // UI State
    private val _isInitialized = MutableLiveData<Boolean>(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    private val _permissionsGranted = MutableLiveData<Boolean>(false)
    val permissionsGranted: LiveData<Boolean> = _permissionsGranted

    private val _showEmergencyMode = MutableLiveData<Boolean>(false)
    val showEmergencyMode: LiveData<Boolean> = _showEmergencyMode

    // AI Online/Offline Status
    private val _isAIOnline = MutableLiveData<Boolean>(false)
    val isAIOnline: LiveData<Boolean> = _isAIOnline

    /**
     * Initialize voice assistant
     */
    fun initialize() {
        viewModelScope.launch {
            try {
                // Try to create voice assistant safely
                voiceAssistant = VoiceAssistantManager(getApplication())

                // If successful, connect the state flows
                voiceAssistant?.let { assistant ->
                    // Observe the assistant's state and forward to our LiveData
                    viewModelScope.launch {
                        assistant.assistantState.collect { state ->
                            _assistantState.postValue(state)
                        }
                    }

                    assistant.currentResponse.observeForever { response ->
                        _currentResponse.postValue(response)
                    }
                    assistant.recognizedText.observeForever { text ->
                        _recognizedText.postValue(text)
                    }
                    assistant.errorMessage.observeForever { error ->
                        _errorMessage.postValue(error)
                    }

                    assistant.initialize { success ->
                        if (success) {
                            _isInitialized.postValue(true)
                            // Check if AI is actually online (has valid API connection)
                            checkAIStatus(assistant)
                        } else {
                            _errorMessage.postValue("AI features unavailable - using offline mode")
                            _isInitialized.postValue(true) // Still allow basic functionality
                            _isAIOnline.postValue(false) // AI is offline
                        }
                    }
                } ?: run {
                    _errorMessage.postValue("AI features unavailable - using offline mode")
                    _isInitialized.postValue(true) // Still allow basic functionality
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Voice assistant not available: ${e.message}")
                _isInitialized.postValue(false)
            }
        }
    }

    /**
     * Check if AI is actually online and update status
     */
    private fun checkAIStatus(assistant: VoiceAssistantManager) {
        viewModelScope.launch {
            try {
                // Check if BuildConfig has valid Gemini API key
                val buildConfigClass = Class.forName("${getApplication<Application>().packageName}.BuildConfig")
                val field = buildConfigClass.getDeclaredField("GEMINI_API_KEY")
                val apiKey = field.get(null) as? String

                val hasValidAI = !apiKey.isNullOrBlank() && apiKey.length > 10
                _isAIOnline.postValue(hasValidAI)

                if (!hasValidAI) {
                    _errorMessage.postValue("AI running in offline mode - basic responses available")
                }
            } catch (e: Exception) {
                _isAIOnline.postValue(false)
                _errorMessage.postValue("AI offline - using fallback responses")
            }
        }
    }

    /**
     * Start listening for voice commands
     */
    fun startListening() {
        voiceAssistant?.startListening() ?: run {
            _errorMessage.postValue("Voice assistant not available")
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        voiceAssistant?.stopListening()
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        voiceAssistant?.stopSpeaking()
    }

    /**
     * Handle emergency command directly
     */
    fun handleEmergencyCommand(commandType: VoiceCommandType) {
        voiceAssistant?.handleEmergencyCommand(commandType) ?: run {
            // Provide offline emergency guidance
            val offlineResponse = getOfflineEmergencyResponse(commandType)
            _currentResponse.postValue(offlineResponse)
            _errorMessage.postValue("Using offline emergency guidance")
        }
    }

    /**
     * Provide offline emergency responses when AI is not available
     */
    private fun getOfflineEmergencyResponse(commandType: VoiceCommandType): VoiceResponse {
        val responseText = when (commandType) {
            VoiceCommandType.EMERGENCY_CPR -> {
                """TRAINING REFERENCE - CPR:
1. Check responsiveness - tap shoulders, shout "Are you okay?"
2. Call 112 immediately or have someone else call
3. Place on firm surface, tilt head back, lift chin
4. Place heel of hand on center of chest, between nipples
5. Push hard and fast at least 2 inches deep
6. Compress at rate of 100-120 per minute
7. Allow complete chest recoil between compressions
8. Continue until help arrives"""
            }
            VoiceCommandType.EMERGENCY_CHOKING -> {
                """TRAINING REFERENCE - CHOKING:
1. Ask "Are you choking?" If they can't speak or breathe:
2. Stand behind person, wrap arms around waist
3. Make fist, place thumb side against upper abdomen
4. Grasp fist with other hand, thrust upward forcefully
5. Repeat until object is expelled or person becomes unconscious
6. If unconscious, call 112 and begin CPR
7. For infants: 5 back blows, then 5 chest thrusts"""
            }
            VoiceCommandType.EMERGENCY_BLEEDING -> {
                """TRAINING REFERENCE - BLEEDING CONTROL:
1. Apply direct pressure with clean cloth or bandage
2. Press firmly over wound, don't remove cloth if soaked
3. Add more layers on top if bleeding continues
4. Elevate injured area above heart if possible
5. Apply pressure to pressure points if bleeding severe
6. Call 112 for severe bleeding
7. Watch for signs of shock"""
            }
            VoiceCommandType.EMERGENCY_BURNS -> {
                """TRAINING REFERENCE - BURN CARE:
1. Remove from heat source immediately
2. Cool burn with cool (not ice cold) running water 10-20 minutes
3. Remove jewelry/clothing before swelling occurs
4. Cover with sterile, non-adhesive bandage
5. Do NOT use ice, butter, or ointments
6. For serious burns: Call 112 immediately
7. Watch for signs of shock"""
            }
            else -> "Training guidance not available offline. Please call 112 for immediate help."
        }

        return VoiceResponse(
            text = responseText,
            metadata = mapOf(
                "source" to "offline",
                "urgency" to "HIGH",
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    /**
     * Quick emergency actions
     */
    fun startCPRGuidance() {
        handleEmergencyCommand(VoiceCommandType.EMERGENCY_CPR)
        _showEmergencyMode.value = true
    }

    fun startChokingGuidance() {
        handleEmergencyCommand(VoiceCommandType.EMERGENCY_CHOKING)
        _showEmergencyMode.value = true
    }

    fun startBleedingGuidance() {
        handleEmergencyCommand(VoiceCommandType.EMERGENCY_BLEEDING)
        _showEmergencyMode.value = true
    }

    fun startBurnsGuidance() {
        handleEmergencyCommand(VoiceCommandType.EMERGENCY_BURNS)
        _showEmergencyMode.value = true
    }

    /**
     * Exit emergency mode
     */
    fun exitEmergencyMode() {
        _showEmergencyMode.value = false
        voiceAssistant?.let { assistant ->
            assistant.clearConversationHistory()
            assistant.stopSpeaking()
            assistant.stopListening()
        }
    }

    /**
     * Provide voice feedback for emergency procedures
     */
    fun provideFeedback(message: String) {
        viewModelScope.launch {
            try {
                voiceAssistant?.speakResponse(message) ?: run {
                    // Fallback: Show text feedback if TTS is not available
                    _currentResponse.postValue(
                        VoiceResponse(
                            text = message,
                            metadata = mapOf("source" to "feedback")
                        )
                    )
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Voice feedback unavailable: ${e.message}")
            }
        }
    }

    /**
     * Update voice preferences
     */
    fun updatePreferences(preferences: VoicePreferences) {
        voiceAssistant?.updatePreferences(preferences) ?: run {
            _errorMessage.postValue("Cannot update preferences - voice assistant not available")
        }
    }

    /**
     * Check permissions - always checks actual permission state
     */
    fun checkPermissions() {
        // Create a temporary permission manager to check current state
        val tempPermissionManager = VoicePermissionManager(getApplication())
        _permissionsGranted.value = tempPermissionManager.areAllPermissionsGranted()
    }

    /**
     * Get missing permissions - always checks actual permission state
     */
    fun getMissingPermissions(): List<String> {
        // Create a temporary permission manager to check current state
        val tempPermissionManager = VoicePermissionManager(getApplication())
        return tempPermissionManager.getMissingPermissions()
    }

    /**
     * Get permission rationale
     */
    fun getPermissionRationale(permission: String): String {
        val tempPermissionManager = VoicePermissionManager(getApplication())
        return tempPermissionManager.getPermissionRationale(permission)
    }

    /**
     * Clear conversation and start fresh
     */
    fun clearConversation() {
        voiceAssistant?.clearConversationHistory()
    }

    /**
     * Manually shutdown voice assistant (called from fragment onDestroyView)
     */
    fun shutdown() {
        voiceAssistant?.shutdown()
    }

    /**
     * Cleanup
     */
    override fun onCleared() {
        super.onCleared()
        voiceAssistant?.shutdown()
    }
}

