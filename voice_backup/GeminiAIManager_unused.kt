package com.example.firstaidapp.voice

// Backup of unused members from GeminiAIManager
// Created: 2025-10-27

import com.example.firstaidapp.data.voice.VoiceResponse

// Previously-unused field
// private val emergencyProcedures = mutableListOf<String>()

// Previously-unused flag
// private var isAIModelReady: Boolean = false

// Previously-unused (legacy) audio processing method
// suspend fun processVoiceCommandWithAudio(audioData: ByteArray): Result<VoiceResponse> = withContext(Dispatchers.IO) {
//     try {
//         Log.d(tag, "Processing audio input via Live API")
//
//         // In a real implementation, this would use speech-to-text
//         // For now, return a response indicating audio was received
//         Result.success(VoiceResponse(
//             text = "I received your audio. Please describe your emergency situation clearly.",
//             metadata = mapOf(
//                 "source" to "live_api_audio",
//                 "processing" to "true",
//                 "timestamp" to System.currentTimeMillis().toString()
//             )
//         ))
//     } catch (e: Exception) {
//         Log.e(tag, "Error processing audio input", e)
//         processFallbackCommand("voice input received")
//     }
// }

