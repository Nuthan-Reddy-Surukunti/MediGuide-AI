# Android Text-to-Speech (TTS) — Workflow & Integration Guide

Last updated: October 28, 2025

This document explains how to call and integrate Android's TextToSpeech (TTS) API, the typical lifecycle, best practices, a compact Kotlin example, and integration tips for the Voice Assistant UI found in this project.

---

## Quick summary

- TextToSpeech is initialized asynchronously. You must wait for onInit(SUCCESS) before calling speak or synthesize.
- Use setLanguage / isLanguageAvailable to verify locale support.
- Use OnUtteranceProgressListener (or UtteranceProgressListener) to get start/done/error callbacks for utterances.
- For API 21+, prefer the speak(text, queueMode, bundle, utteranceId) signature and set AudioAttributes/Voice.
- Always call shutdown() when the TTS instance is no longer needed.

---

## 1. Call & lifecycle (high-level)

1. Create an instance: `TextToSpeech(context, onInitListener)`.
2. Wait for `onInit(status)` and check `status == TextToSpeech.SUCCESS`.
3. Configure language, pitch, rate, audio attributes, and optionally select a `Voice`.
4. Register a `UtteranceProgressListener` to receive `onStart`, `onDone`, and `onError` for each utterance (use unique utterance IDs to track them).
5. Call `speak()` or `synthesizeToFile()` to produce audio.
6. Use `stop()` to interrupt and `shutdown()` to free resources.

Notes:
- Initialization is asynchronous — never call `speak()` before success.
- `setLanguage()` returns codes: `LANG_AVAILABLE`, `LANG_MISSING_DATA`, `LANG_NOT_SUPPORTED`.
- Some engines require the user to install voice/data packs; handle `ACTION_CHECK_TTS_DATA` / `ACTION_INSTALL_TTS_DATA` if needed.

---

## 2. Core API calls & parameters (concise)

- Constructor: `TextToSpeech(context, TextToSpeech.OnInitListener)` (or with engine name).
- Set language: `tts.setLanguage(locale)`.
- Set voice (API 21+): `tts.setVoice(voice)` with `tts.voices`.
- Set pitch / rate: `tts.setPitch(float)`, `tts.setSpeechRate(float)`.
- Speak (API 21+): `tts.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)`.
- Synthesize to file (API 21+): `tts.synthesizeToFile(text, bundle, file, utteranceId)`.
- Progress listener: `tts.setOnUtteranceProgressListener(listener)`.
- Shutdown: `tts.shutdown()`.

---

## 3. Compact Kotlin manager example

This pattern encapsulates TTS lifecycle and exposes simple methods for the rest of the app. The snippet below is a compact manager you can adopt and expand in your project.

```kotlin
class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                val locale = Locale.getDefault()
                val res = tts?.setLanguage(locale)
                // handle missing/unsupported language
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val aa = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(aa)
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) { /* update UI */ }
                    override fun onDone(utteranceId: String) { /* update UI */ }
                    override fun onError(utteranceId: String) { /* handle error */ }
                })
            } else {
                // initialization failed — prompt user to install TTS data or handle fallback
            }
        }
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!ready) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
        }
    }

    fun synthesizeToFile(text: String, outFile: File, utteranceId: String = "synth-${System.currentTimeMillis()}") {
        if (!ready) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            tts?.synthesizeToFile(text, params, outFile, utteranceId)
        } else {
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
            @Suppress("DEPRECATION")
            tts?.synthesizeToFile(text, params, outFile.absolutePath)
        }
    }

    fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown(); tts = null; ready = false }
}
```

Notes:
- Use unique utterance IDs to correlate progress callbacks with UI changes.
- `synthesizeToFile` requires write access if writing to external storage.

---

## 4. UtteranceProgressListener (callbacks & UI integration)

- onStart(utteranceId): speaking started — set UI to `Speaking` state, show `speakingAnimation`.
- onDone(utteranceId): finished — set UI back to `Idle` or next queued state, hide animation.
- onError(utteranceId): show error and fallback UI.

Use these to update the `VoiceAssistantState` that's already driving your UI. For example, when `onStart` is called for a response utterance, update a LiveData flag `isSpeaking = true` and when `onDone` set `isSpeaking = false`.

---

## 5. Integration tips for this project (Voice Assistant)

File: `VoiceAssistantFragment.kt` — suggested usage:

- Initialize a single shared `TtsManager` (or TTS instance) when the fragment/activity starts (e.g., in `onCreate` or `onViewCreated`).
- Expose methods in your ViewModel to request speech for a given response text; ViewModel calls `ttsManager.speak(...)` or the Fragment observes response LiveData and calls `ttsManager.speak(...)`.
- Map TTS progress to UI states in `VOICE_AI_UI_MAPPING.md`:
  - When TTS `onStart` → set `VoiceAssistantState.Speaking` → show `speakingAnimation` and update `statusText` to "Speaking...".
  - When TTS `onDone` → set `VoiceAssistantState.Idle` (or next appropriate state) → hide animation and update `statusText`.
  - `btnStop` should call `ttsManager.stop()` and set UI state to `Idle`.

Example binding points (from your UI mapping):
- `btnStop.setOnClickListener { ttsManager.stop(); viewModel.stopSpeaking() }`
- When user triggers emergency flow, pipe the step text through `ttsManager.speak(stepText, utteranceId)` and use the utterance callbacks to advance steps or mark completion.

---

## 6. Common edge cases & recommendations

- Initialization race: guard all speak/synthesize calls behind a `ready`/`initialized` flag.
- Unsupported locale: check `setLanguage` return value and provide a fallback language or prompt install.
- Missing TTS engine/data: prompt user with `TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA` or open settings.
- Long texts: some TTS engines choke on very long inputs — chunk long text into smaller pieces and queue them (`QUEUE_ADD`) with controlled delays if needed.
- Audio focus: request audio focus (AudioManager/AudioAttributes) to avoid being mixed with media audio, and abandon focus when done.
- Background service: be careful with lifecycle. If TTS runs in a background Service, ensure you manage audio focus and call `shutdown()` when the service stops.
- Permissions: no runtime permissions required for TTS itself. Writing synthesized files to external storage requires storage permission or use app-specific storage.

---

## 7. Testing checklist

- Test on devices with different TTS engines (Google TTS, OEM TTS).
- Test unsupported languages and missing data flows.
- Test `synthesizeToFile` and validate saved audio file playback.
- Test interruption (`stop()`), queueing (`QUEUE_ADD`) and `UtteranceProgressListener` callbacks.
- Test audio focus interactions with music and phone calls.

---

## 8. Next steps / suggestions

- Add a `TtsManager` Kotlin file under `app/src/main/java/...` and wire it into `VoiceAssistantFragment.kt`.
- Add instrumentation/unit tests around your ViewModel to verify it calls the TTS manager via an interface (use an interface to mock TTS during tests).
- Add UI tests that validate the `speaking` animation appears while TTS is speaking (use Espresso idling resources tied to utterance callbacks).

---

If you want, I can now:
- Add a `TtsManager.kt` file directly into the project and wire basic calls in `VoiceAssistantFragment.kt` (I can search and patch the fragment), or
- Generate a small unit-testable wrapper interface for TTS and add tests.

Tell me which of these you'd like next, and I will implement it.

