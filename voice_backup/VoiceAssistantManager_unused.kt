package com.example.firstaidapp.voice

// Backup of unused members from VoiceAssistantManager
// Created: 2025-10-27

// Legacy live session APIs that are currently not referenced by UI
// suspend fun startLiveVoiceSession(): Result<String> {
//     return try {
//         _assistantState.value = VoiceAssistantState.Processing
//
//         val result = geminiAI.startVoiceConversation()
//
//         result.onSuccess { message ->
//             _assistantState.value = VoiceAssistantState.Speaking
//             Log.d(tag, "Live voice session started: $message")
//
//             // Speak welcome message for live session
//             textToSpeech.speak("Live emergency assistant activated. Describe your emergency situation clearly.")
//         }
//
//         result.onFailure { error ->
//             _assistantState.value = VoiceAssistantState.Error(error.message ?: "Failed to start live session")
//             Log.e(tag, "Failed to start live session", error)
//         }
//
//         result
//     } catch (e: Exception) {
//         Log.e(tag, "Error starting live voice session", e)
//         Result.failure(e)
//     }
// }
//
// suspend fun stopLiveVoiceSession(): Result<String> {
//     return try {
//         val result = geminiAI.stopVoiceConversation()
//
//         result.onSuccess { message ->
//             _assistantState.value = VoiceAssistantState.Idle
//             Log.d(tag, "Live voice session stopped: $message")
//         }
//
//         result
//     } catch (e: Exception) {
//         Log.e(tag, "Error stopping live voice session", e)
//         Result.failure(e)
//     }
// }

// Callback handlers that are used, but kept here as backup in case of refactor
// private suspend fun handleEmergencyCall(emergencyType: String, urgency: String) { ... }
// private suspend fun handleProcedureNavigation(procedureName: String) { ... }

