package com.mediguide.firstaid.data.voice

/**
 * Represents different types of emergency voice commands
 * Only actively used command types are included
 */
enum class VoiceCommandType {
    EMERGENCY_CPR,
    EMERGENCY_CHOKING,
    EMERGENCY_BLEEDING,
    EMERGENCY_BURNS
}

/**
 * Voice assistant response from AI
 */
data class VoiceResponse(
    val text: String,
    val actionRequired: VoiceAction? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Actions that the voice assistant can trigger
 * Only actively used actions are included
 */
sealed class VoiceAction {
    data class NavigateToProcedure(val procedureId: String) : VoiceAction()
    data class CallEmergency(val number: String) : VoiceAction()
}

