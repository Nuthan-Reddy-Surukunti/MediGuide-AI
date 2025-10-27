package com.example.firstaidapp.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.content.Intent
import android.util.Log
import java.util.*

/**
 * Simple voice helper that replaces 5 complex voice classes:
 * - TextToSpeechManager (150+ lines)
 * - SpeechRecognitionService (200+ lines)
 * - VoiceAssistantManager (100+ lines)
 * - VoicePermissionManager (80+ lines)
 * - GeminiAIManager (separate, keep as is)
 *
 * Total reduction: ~530 lines → 60 lines
 */
object VoiceHelper {

    private var tts: TextToSpeech? = null
    private var isReady = false

    /**
     * Initialize text-to-speech (simple version)
     */
    fun initTTS(context: Context, onReady: () -> Unit = {}) {
        if (tts != null) return

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isReady = true
                onReady()
            }
        }
    }

    /**
     * Speak text (simple version)
     */
    fun speak(text: String) {
        if (isReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    /**
     * Stop speaking
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * Get speech recognition intent (simple version)
     */
    fun getSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
        }
    }

    /**
     * Check if microphone permission is granted
     */
    fun hasMicPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
               android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
