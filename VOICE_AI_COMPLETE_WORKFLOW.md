# Voice AI Complete Workflow Documentation

> **Last Updated:** October 28, 2025  
> **Purpose:** End-to-end workflow documentation from UI interaction to backend processing

---

## 🚀 Complete Workflow: From Button Click to Response

This document traces the complete journey of a voice interaction through the entire system, including both **online (AI-powered)** and **offline (fallback)** modes.

---

## 📋 Table of Contents
1. [User Clicks Microphone Button](#1-user-clicks-microphone-button)
2. [Speech Recognition](#2-speech-recognition)
3. [AI Processing (Online Mode)](#3-ai-processing-online-mode)
4. [Fallback Processing (Offline Mode)](#4-fallback-processing-offline-mode)
5. [Text-to-Speech Response](#5-text-to-speech-response)
6. [Emergency Quick Actions Workflow](#6-emergency-quick-actions-workflow)
7. [State Management Throughout](#7-state-management-throughout)

---

## 1. User Clicks Microphone Button

### **Frontend (UI Layer)**

#### **File:** `VoiceAssistantFragment.kt`
**Line:** 68-70

```kotlin
binding.microphoneButton.setOnClickListener {
    handleMicrophoneClick()
}
```

#### **What Happens:**
1. User taps the FloatingActionButton with id `microphoneButton`
2. Click listener invokes `handleMicrophoneClick()`

---

#### **File:** `VoiceAssistantFragment.kt`
**Method:** `handleMicrophoneClick()`
**Line:** 282-284

```kotlin
private fun handleMicrophoneClick() {
    viewModel.startListening()
}
```

#### **What Happens:**
1. Method delegates to ViewModel
2. No UI logic here - follows MVVM pattern

---

### **ViewModel Layer**

#### **File:** `VoiceAssistantViewModel.kt`
**Method:** `startListening()`
**Lines:** 129-135

```kotlin
fun startListening() {
    voiceAssistant?.startListening() ?: run {
        _errorMessage.postValue("Voice assistant not available")
    }
}
```

#### **What Happens:**
1. Checks if `voiceAssistant` is initialized
2. If yes → calls `VoiceAssistantManager.startListening()`
3. If no → posts error message to UI

---

### **Backend (Manager Layer)**

#### **File:** `VoiceAssistantManager.kt`
**Method:** `startListening()`
**Lines:** 146-185

```kotlin
fun startListening() {
    if (!isInitialized) {
        Log.w(tag, "Voice assistant not initialized")
        _errorMessage.postValue("Voice assistant not ready")
        return
    }

    if (_assistantState.value is VoiceAssistantState.Listening) {
        Log.w(tag, "Already listening")
        return
    }

    // Stop any current speech
    textToSpeech.stop()

    _assistantState.value = VoiceAssistantState.Listening
    Log.d(tag, "Started listening for voice commands")

    speechRecognition.startListening(
        onResult = { text ->
            handleRecognizedSpeech(text)
        },
        onError = { error ->
            Log.e(tag, "Speech recognition error: $error")
            _assistantState.value = VoiceAssistantState.Error(error)
            _errorMessage.postValue(error)
            // Auto-retry for common errors
            if (error.contains("No speech") || error.contains("timeout")) {
                scope.launch {
                    delay(1000)
                    if (_assistantState.value is VoiceAssistantState.Error) {
                        _assistantState.value = VoiceAssistantState.Idle
                    }
                }
            }
        },
        onReadyForSpeech = {
            Log.d(tag, "Ready for speech input")
        },
        onPartialResult = { partialText ->
            _recognizedText.postValue(partialText)
        }
    )
}
```

#### **What Happens:**
1. **Validation checks:**
   - Is voice assistant initialized?
   - Are we already listening?
2. **Preparation:**
   - Stops any ongoing TTS
   - Updates state to `Listening`
3. **Delegates to SpeechRecognitionService** with callbacks:
   - `onResult` → Process final recognized text
   - `onError` → Handle errors with auto-retry
   - `onReadyForSpeech` → Log readiness
   - `onPartialResult` → Show partial text in UI

---

## 2. Speech Recognition

### **Backend (Speech Service Layer)**

#### **File:** `SpeechRecognitionService.kt`
**Method:** `startListening()`
**Lines:** 32-100

```kotlin
fun startListening(
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
    onReadyForSpeech: () -> Unit = {},
    onPartialResult: (String) -> Unit = {}
) {
    if (speechRecognizer == null) {
        initialize()
    }

    if (isListening) {
        Log.w(tag, "Already listening")
        return
    }

    val intent = createRecognitionIntent()

    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            Log.d(tag, "Ready for speech")
            onReadyForSpeech()
        }

        override fun onBeginningOfSpeech() {
            Log.d(tag, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Real-time audio level feedback
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }

        override fun onEndOfSpeech() {
            isListening = false
            Log.d(tag, "End of speech")
        }

        override fun onError(error: Int) {
            isListening = false
            val errorMessage = getErrorMessage(error)
            Log.e(tag, "Recognition error: $errorMessage")
            onError(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                Log.d(tag, "Recognition result: $recognizedText")
                onResult(recognizedText)
            } else {
                onError("No speech recognized")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            if (!matches.isNullOrEmpty()) {
                val partialText = matches[0]
                Log.d(tag, "Partial result: $partialText")
                onPartialResult(partialText)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Additional events
        }
    })

    speechRecognizer?.startListening(intent)
    Log.d(tag, "Started listening")
}
```

#### **What Happens:**
1. **Initialize Android SpeechRecognizer** if not already done
2. **Create recognition intent** with optimal settings:
   - Language model: FREE_FORM (natural speech)
   - Partial results: Enabled
   - Silence timeouts: 2 seconds
3. **Set up RecognitionListener** for callbacks:
   - `onReadyForSpeech` → Update UI
   - `onBeginningOfSpeech` → Speech detected
   - `onPartialResults` → Show live transcription
   - `onResults` → Final recognized text
   - `onError` → Handle errors
   - `onEndOfSpeech` → Speech ended
4. **Start listening** via Android Speech API

#### **UI Updates (via callbacks):**
```
onReadyForSpeech → State: Listening → UI shows "Listening..."
onPartialResult → Updates tvRecognizedText in real-time
onResults → Triggers AI processing
onError → Shows error, auto-retries common errors
```

---

## 3. AI Processing (Online Mode)

### **Step 3A: Speech Recognized → Process Command**

#### **File:** `VoiceAssistantManager.kt`
**Method:** `handleRecognizedSpeech()`
**Lines:** 196-230

```kotlin
private fun handleRecognizedSpeech(text: String) {
    Log.d(tag, "Processing recognized speech: $text")
    _recognizedText.postValue(text)
    _assistantState.value = VoiceAssistantState.Processing

    scope.launch {
        try {
            // Send to Gemini AI for processing
            val result = geminiAI.processVoiceCommand(text)

            result.onSuccess { response ->
                _currentResponse.postValue(response)

                // Speak the response
                if (currentPreferences.autoSpeak) {
                    speakResponse(response.text)
                } else {
                    _assistantState.value = VoiceAssistantState.Idle
                }

                // Handle any required actions
                response.actionRequired?.let { action ->
                    handleVoiceAction(action)
                }
            }

            result.onFailure { error ->
                Log.e(tag, "AI processing error", error)
                _assistantState.value = VoiceAssistantState.Error(error.message ?: "Processing failed")
                _errorMessage.postValue(error.message ?: "Failed to process command")

                // Fallback to basic command processing
                handleBasicCommand(text)
            }

        } catch (e: Exception) {
            Log.e(tag, "Error handling speech", e)
            _assistantState.value = VoiceAssistantState.Error(e.message ?: "Unknown error")
            _errorMessage.postValue(e.message ?: "An error occurred")
        }
    }
}
```

#### **What Happens:**
1. **Updates UI** with recognized text
2. **Changes state** to `Processing`
3. **Launches coroutine** for async AI processing
4. **Sends to Gemini AI** via `geminiAI.processVoiceCommand(text)`
5. **On Success:**
   - Posts response to UI
   - Speaks response if auto-speak enabled
   - Handles required actions (navigation, emergency calls)
6. **On Failure:**
   - Logs error
   - Falls back to basic command processing (offline mode)

---

### **Step 3B: Gemini AI Processing**

#### **File:** `GeminiAIManager.kt`
**Method:** `processVoiceCommand()` → `processEmergencyVoiceInput()`
**Lines:** 132-205

```kotlin
suspend fun processEmergencyVoiceInput(text: String): Result<VoiceResponse> = withContext(Dispatchers.IO) {
    try {
        if (!isModelInitialized.get() || generativeModel == null) {
            return@withContext processFallbackCommand(text)
        }

        val emergencyPrompt = """
            ${getEmergencyFirstAidPrompt()}
            
            EMERGENCY SITUATION: $text
            
            Analyze this emergency and provide immediate first aid guidance. 
            If this is life-threatening, recommend calling 911 immediately.
            Provide clear, step-by-step instructions that can be followed during panic.
        """.trimIndent()

        // Use Google AI Client to generate content
        val response = generativeModel!!.generateContent(
            content {
                text(emergencyPrompt)
            }
        )

        val responseText = response.text ?: getBasicEmergencyResponse(text)

        // Analyze for emergency actions needed
        val actionRequired = when {
            responseText.contains("call 911", ignoreCase = true) ||
            responseText.contains("call emergency", ignoreCase = true) -> {
                onEmergencyCall?.invoke("medical", "HIGH")
                VoiceAction.CallEmergency("911")
            }
            responseText.contains("navigate to", ignoreCase = true) -> {
                val procedureName = extractProcedureName(text)
                onNavigateToProcedure?.invoke(procedureName)
                VoiceAction.NavigateToProcedure(procedureName)
            }
            else -> null
        }

        // Determine urgency level
        val urgency = when {
            responseText.contains("immediately", ignoreCase = true) ||
            responseText.contains("911", ignoreCase = true) -> "CRITICAL"
            responseText.contains("urgent", ignoreCase = true) ||
            responseText.contains("quickly", ignoreCase = true) -> "HIGH"
            else -> "MEDIUM"
        }

        Result.success(VoiceResponse(
            text = responseText,
            actionRequired = actionRequired,
            metadata = mapOf(
                "source" to "gemini_ai",
                "urgency" to urgency,
                "timestamp" to System.currentTimeMillis().toString()
            )
        ))
    } catch (e: Exception) {
        Log.e(tag, "Error processing emergency voice input with AI", e)
        processFallbackCommand(text)
    }
}
```

#### **What Happens:**
1. **Check AI availability:**
   - Is model initialized?
   - Is API key valid?
2. **Build emergency prompt** with:
   - System instructions for first aid
   - User's emergency situation
   - Request for immediate guidance
3. **Call Gemini API:**
   - Uses `gemini-2.0-flash` model
   - Generates content based on prompt
4. **Analyze response:**
   - Extract action requirements (call 911, navigate)
   - Determine urgency level (CRITICAL, HIGH, MEDIUM)
   - Build VoiceResponse with metadata
5. **Return Result:**
   - Success → VoiceResponse with AI guidance
   - Failure → Falls back to offline mode

---

### **Step 3C: Emergency Prompt System**

#### **File:** `GeminiAIManager.kt`
**Method:** `getEmergencyFirstAidPrompt()`
**Lines:** 223-256

```kotlin
private fun getEmergencyFirstAidPrompt(): String {
    return """
        You are an AI Emergency First Aid Assistant. Your role is to provide IMMEDIATE, SPECIFIC guidance based on the exact situation described.

        CRITICAL INSTRUCTION: Analyze the specific emergency situation first, then provide ONLY the most relevant, immediate actions needed for that specific case. DO NOT provide generic checklists or comprehensive procedures unless specifically needed.

        RESPONSE APPROACH:
        1. ANALYZE THE SPECIFIC SITUATION: What exactly is happening? What are the key symptoms/conditions mentioned?
        2. DETERMINE IMMEDIATE PRIORITY: What is the most urgent action needed right now?
        3. PROVIDE TARGETED GUIDANCE: Give specific steps for THIS situation, not general emergency procedures
        4. BE CONCISE BUT COMPLETE: Focus on what matters most for this specific emergency

        RESPONSE STRUCTURE:
        - IMMEDIATE ACTION (1-2 sentences): What to do RIGHT NOW
        - SPECIFIC STEPS (3-5 steps max): Targeted to this exact situation  
        - CALL 911 WHEN: Only mention if truly necessary for this specific case
        - MONITOR FOR: What to watch for specifically in this situation

        EXAMPLES OF GOOD RESPONSES:
        - If "unconscious": "Check for breathing immediately. If not breathing, start CPR now - 30 chest compressions, 2 breaths."
        - If "choking": "Stand behind them and give 5 firm back blows between shoulder blades. If object doesn't come out, do abdominal thrusts."
        - If "chest pain": "Have them sit down and rest immediately. Call 911 now. Give aspirin if available and no allergies."

        AVOID:
        - Long comprehensive lists when situation is specific
        - Multiple "if this, then that" scenarios unless needed
        - Generic emergency overviews
        - Repeating information not relevant to the described situation

        TONE: Calm, authoritative, focused. Speak as if you're right there helping with THIS specific emergency.

        Remember: The person is in a specific emergency situation. Give them exactly what they need for their situation, not everything they might possibly need.
    """
}
```

#### **What This Does:**
- **Guides AI behavior** for emergency-specific responses
- **Ensures concise, actionable** guidance
- **Prevents generic responses** 
- **Optimizes for panic situations**

---

## 4. Fallback Processing (Offline Mode)

### **When Offline Mode Triggers:**
1. No internet connection
2. Gemini API key missing/invalid
3. AI model initialization failed
4. AI processing error/timeout

---

### **Step 4A: Offline Command Detection**

#### **File:** `GeminiAIManager.kt`
**Method:** `processFallbackCommand()`
**Lines:** 333-384

```kotlin
private suspend fun processFallbackCommand(command: String): Result<VoiceResponse> {
    return try {
        val lowerCommand = command.lowercase()

        // Check for known emergency keywords and provide basic responses
        val response = when {
            lowerCommand.contains("cpr") || lowerCommand.contains("cardiac") -> {
                emergencyResponses["cpr"]!!
            }
            lowerCommand.contains("choking") || lowerCommand.contains("heimlich") -> {
                emergencyResponses["choking"]!!
            }
            lowerCommand.contains("bleeding") || lowerCommand.contains("blood") -> {
                emergencyResponses["bleeding"]!!
            }
            lowerCommand.contains("burn") -> {
                emergencyResponses["burns"]!!
            }
            lowerCommand.contains("heart attack") -> {
                emergencyResponses["heart_attack"]!!
            }
            lowerCommand.contains("stroke") -> {
                emergencyResponses["stroke"]!!
            }
            lowerCommand.contains("fracture") || lowerCommand.contains("broken") -> {
                emergencyResponses["fracture"]!!
            }
            else -> {
                VoiceResponse(
                    text = "I'm operating in offline mode. For any life-threatening emergency, call 911 immediately. Can you describe your specific emergency situation?",
                    metadata = mapOf(
                        "source" to "offline_mode",
                        "urgency" to "MEDIUM",
                        "timestamp" to System.currentTimeMillis().toString()
                    )
                )
            }
        }

        Result.success(response)
    } catch (e: Exception) {
        Log.e(tag, "Error in fallback command processing", e)
        Result.failure(e)
    }
}
```

#### **What Happens:**
1. **Keyword matching** on user input
2. **Returns pre-defined responses** from `emergencyResponses` map
3. **Each response includes:**
   - Specific first aid instructions
   - Action requirements (call 911, navigate)
   - Urgency metadata

---

### **Step 4B: Pre-defined Emergency Responses**

#### **File:** `GeminiAIManager.kt`
**Property:** `emergencyResponses`
**Lines:** 28-73

```kotlin
private val emergencyResponses = mapOf(
    "cpr" to VoiceResponse(
        text = "Start CPR immediately. Place hands on center of chest. Push hard and fast at least 2 inches deep, 100-120 compressions per minute. Call 911 now!",
        actionRequired = VoiceAction.CallEmergency("911"),
        metadata = mapOf("urgency" to "HIGH", "procedure" to "CPR")
    ),
    "choking" to VoiceResponse(
        text = "For choking: Stand behind person, place hands below ribcage, thrust upward and inward 5 times. Repeat until object comes out or person becomes unconscious.",
        actionRequired = VoiceAction.NavigateToProcedure("choking"),
        metadata = mapOf("urgency" to "HIGH", "procedure" to "HEIMLICH")
    ),
    "bleeding" to VoiceResponse(
        text = "Control bleeding: Apply direct pressure with clean cloth. Keep pressure constant. Elevate the injured area above heart level if possible.",
        actionRequired = VoiceAction.NavigateToProcedure("bleeding"),
        metadata = mapOf("urgency" to "HIGH", "procedure" to "BLEEDING_CONTROL")
    ),
    // ... more responses
)
```

#### **Offline Response Structure:**
```
User says: "Someone is choking"
         ↓
Keyword match: "choking"
         ↓
Return: emergencyResponses["choking"]
         ↓
Response includes:
├── text: Step-by-step Heimlich instructions
├── actionRequired: Navigate to choking procedure
└── metadata: urgency=HIGH, procedure=HEIMLICH
```

---

## 5. Text-to-Speech Response

### **Step 5A: Speak Response**

#### **File:** `VoiceAssistantManager.kt`
**Method:** `speakResponse()`
**Lines:** 241-262

```kotlin
fun speakResponse(text: String, onComplete: (() -> Unit)? = null) {
    _assistantState.value = VoiceAssistantState.Speaking

    textToSpeech.speak(
        text = text,
        onStart = {
            Log.d(tag, "Started speaking: $text")
        },
        onDone = {
            Log.d(tag, "Finished speaking")
            _assistantState.value = VoiceAssistantState.Idle
            onComplete?.invoke()
        },
        onError = {
            Log.e(tag, "TTS error")
            _assistantState.value = VoiceAssistantState.Error("Speech output failed")
            _errorMessage.postValue("Failed to speak response")
        }
    )
}
```

#### **What Happens:**
1. **Update state** to `Speaking`
2. **Delegate to TextToSpeechManager**
3. **Callbacks:**
   - `onStart` → Log start
   - `onDone` → Reset to Idle, run completion callback
   - `onError` → Show error

---

### **Step 5B: TTS Manager**

#### **File:** `TextToSpeechManager.kt`
**Method:** `speak()`
**Lines:** 91-134

```kotlin
fun speak(
    text: String,
    queueMode: Int = TextToSpeech.QUEUE_FLUSH,
    utteranceId: String = "emergency_guidance",
    onStart: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    if (!isInitialized || tts == null) {
        Log.w(tag, "TTS not initialized")
        onError?.invoke()
        return
    }

    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Log.d(tag, "Speech started: $utteranceId")
            onStart?.invoke()
        }

        override fun onDone(utteranceId: String?) {
            Log.d(tag, "Speech completed: $utteranceId")
            onDone?.invoke()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            Log.e(tag, "Speech error: $utteranceId")
            onError?.invoke()
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.e(tag, "Speech error: $utteranceId, code: $errorCode")
            onError?.invoke()
        }
    })

    val result = tts?.speak(text, queueMode, null, utteranceId)
    if (result == TextToSpeech.ERROR) {
        Log.e(tag, "Error speaking text: $text")
        onError?.invoke()
    }
}
```

#### **What Happens:**
1. **Check TTS initialized**
2. **Set utterance progress listener**
3. **Call Android TTS API** to speak text
4. **Handle callbacks** for start/done/error

---

### **UI Updates During TTS:**

```
State: Speaking
         ↓
UI Changes:
├── statusText: "Speaking..."
├── speakingAnimation: VISIBLE
├── listeningAnimation: GONE
└── processingAnimation: GONE
```

---

## 6. Emergency Quick Actions Workflow

### **User Clicks Emergency Button (e.g., CPR)**

#### **File:** `VoiceAssistantFragment.kt`
**Lines:** 72-75

```kotlin
binding.btnEmergencyCpr.setOnClickListener {
    viewModel.startCPRGuidance()
}
```

---

#### **File:** `VoiceAssistantViewModel.kt`
**Method:** `startCPRGuidance()`
**Lines:** 231-234

```kotlin
fun startCPRGuidance() {
    handleEmergencyCommand(VoiceCommandType.EMERGENCY_CPR)
    _showEmergencyMode.value = true
}
```

#### **What Happens:**
1. Calls `handleEmergencyCommand()` with CPR type
2. Triggers emergency overlay mode

---

#### **File:** `VoiceAssistantViewModel.kt`
**Method:** `handleEmergencyCommand()`
**Lines:** 126-151

```kotlin
fun handleEmergencyCommand(commandType: VoiceCommandType) {
    voiceAssistant?.handleEmergencyCommand(commandType) ?: run {
        // Provide offline emergency guidance
        val offlineResponse = getOfflineEmergencyResponse(commandType)
        _currentResponse.postValue(offlineResponse)
        _errorMessage.postValue("Using offline emergency guidance")
    }
}
```

---

#### **File:** `VoiceAssistantManager.kt`
**Method:** `handleEmergencyCommand()`
**Lines:** 276-307

```kotlin
fun handleEmergencyCommand(commandType: VoiceCommandType) {
    scope.launch {
        _assistantState.value = VoiceAssistantState.Processing

        // Convert command type to text for processing
        val emergencyText = when (commandType) {
            VoiceCommandType.EMERGENCY_CPR -> "CPR emergency - person not breathing"
            VoiceCommandType.EMERGENCY_CHOKING -> "choking emergency - person can't breathe"
            VoiceCommandType.EMERGENCY_BLEEDING -> "severe bleeding emergency"
            VoiceCommandType.EMERGENCY_BURNS -> "burn injury emergency"
            else -> "emergency situation"
        }

        val result = geminiAI.processEmergencyVoiceInput(emergencyText)
        result.onSuccess { response: VoiceResponse ->
            _currentResponse.postValue(response)
            speakResponse(response.text ?: "Emergency guidance unavailable")

            response.actionRequired?.let { action: VoiceAction ->
                handleVoiceAction(action)
            }
        }

        result.onFailure { error: Throwable ->
            Log.e(tag, "Emergency guidance error", error)
            _errorMessage.postValue(error.message ?: "Failed to get emergency guidance")
            provideFallbackEmergencyGuidance(commandType)
        }
    }
}
```

#### **Emergency Button Flow:**
```
User clicks btnEmergencyCpr
         ↓
viewModel.startCPRGuidance()
         ↓
handleEmergencyCommand(EMERGENCY_CPR)
         ↓
Converts to text: "CPR emergency - person not breathing"
         ↓
Sends to Gemini AI (or offline fallback)
         ↓
Receives response with CPR instructions
         ↓
Speaks response via TTS
         ↓
Shows emergency overlay UI
         ↓
Updates urgency indicator to RED
```

---

## 7. State Management Throughout

### **State Flow Diagram:**

```
┌──────────────────────────────────────────────────────────┐
│                     IDLE STATE                            │
│  - Mic button enabled                                     │
│  - Status: "Tap to speak"                                 │
│  - All animations hidden                                  │
└──────────────┬───────────────────────────────────────────┘
               │
               │ [User taps microphone]
               ▼
┌──────────────────────────────────────────────────────────┐
│                  LISTENING STATE                          │
│  - Mic button active                                      │
│  - Status: "Listening..."                                 │
│  - listeningAnimation VISIBLE                             │
│  - Partial results shown in tvRecognizedText              │
└──────────────┬───────────────────────────────────────────┘
               │
               │ [Speech recognized]
               ▼
┌──────────────────────────────────────────────────────────┐
│                 PROCESSING STATE                          │
│  - Mic button disabled                                    │
│  - Status: "Processing..."                                │
│  - processingAnimation VISIBLE                            │
│  - Full text shown in tvRecognizedText                    │
└──────────────┬───────────────────────────────────────────┘
               │
               │ [AI response received]
               ▼
┌──────────────────────────────────────────────────────────┐
│                  SPEAKING STATE                           │
│  - Mic button enabled                                     │
│  - Status: "Speaking..."                                  │
│  - speakingAnimation VISIBLE                              │
│  - Response shown in tvResponse                           │
│  - Urgency indicator color updated                        │
└──────────────┬───────────────────────────────────────────┘
               │
               │ [Speech complete]
               ▼
┌──────────────────────────────────────────────────────────┐
│                    IDLE STATE                             │
│  - Ready for next interaction                             │
└───────────────────────────────────────────────────────────┘
```

### **Error State Flow:**

```
Any State
    │
    │ [Error occurs]
    ▼
┌──────────────────────────────────────────────────────────┐
│                   ERROR STATE                             │
│  - Status: "Error: {message}"                             │
│  - All animations hidden                                  │
│  - Error message shown via Toast                          │
└──────────────┬───────────────────────────────────────────┘
               │
               │ [Auto-retry or user action]
               ▼
           IDLE STATE
```

---

## 📊 Complete Data Flow Summary

### **Online Mode (AI Powered):**

```
UI Layer → ViewModel → Manager → Speech Service
                                      ↓
                                 Android Speech API
                                      ↓
                                 Recognized Text
                                      ↓
Manager → Gemini AI Manager → Gemini API (gemini-2.0-flash)
                                      ↓
                                 AI Response
                                      ↓
Manager → TTS Manager → Android TTS API
                            ↓
                      Spoken Response
                            ↓
                       UI Updated
```

### **Offline Mode (Fallback):**

```
UI Layer → ViewModel → Manager → Speech Service
                                      ↓
                                 Recognized Text
                                      ↓
Manager → Gemini AI Manager → processFallbackCommand()
                                      ↓
                            Keyword Matching
                                      ↓
                         Pre-defined Response
                                      ↓
Manager → TTS Manager → Android TTS API
                            ↓
                      Spoken Response
                            ↓
                       UI Updated
```

---

## 🔐 Permission Flow

### **Permission Check on Fragment Load:**

```
Fragment.onViewCreated()
         ↓
checkAndRequestPermissions()
         ↓
viewModel.checkPermissions()
         ↓
VoicePermissionManager.areAllPermissionsGranted()
         ↓
    ┌────┴─────┐
    │          │
   YES        NO
    │          │
    │          └→ showPermissionRationaleDialog()
    │                      ↓
    │              permissionLauncher.launch()
    │                      ↓
    │              User grants/denies
    │                      ↓
    └──────────────────────┘
             │
    viewModel.initialize()
             ↓
    Voice Assistant Ready
```

### **Required Permissions:**
1. **RECORD_AUDIO** - Voice input
2. **ACCESS_FINE_LOCATION** - Find nearby hospitals
3. **ACCESS_COARSE_LOCATION** - Location services
4. **CALL_PHONE** - Emergency calls
5. **MODIFY_AUDIO_SETTINGS** - Audio control

---

## 🎯 Key Files Summary

| Layer | File | Responsibility |
|-------|------|----------------|
| **UI** | `VoiceAssistantFragment.kt` | User interaction, view updates |
| **ViewModel** | `VoiceAssistantViewModel.kt` | State management, UI logic |
| **Manager** | `VoiceAssistantManager.kt` | Orchestration, workflow control |
| **AI** | `GeminiAIManager.kt` | AI processing, online/offline logic |
| **Speech Input** | `SpeechRecognitionService.kt` | Android speech recognition |
| **Speech Output** | `TextToSpeechManager.kt` | Text-to-speech output |
| **Permissions** | `VoicePermissionManager.kt` | Permission management |
| **Data Models** | `VoiceCommand.kt` | Data structures |
| **Layout** | `fragment_voice_assistant.xml` | UI definition |

---

## 🚨 Emergency Mode Special Flow

When user clicks emergency button OR voice command is critical:

```
Emergency Detected
         ↓
_showEmergencyMode.value = true
         ↓
Fragment Observer Triggered
         ↓
UI Changes:
├── normalModeLayout.visibility = GONE
├── emergencyOverlay.visibility = VISIBLE
├── Shows step-by-step emergency instructions
├── Urgency indicator turns RED
└── Speaking animation active
         ↓
TTS speaks emergency instructions
         ↓
User follows guidance
         ↓
User clicks "Exit Emergency" button
         ↓
viewModel.exitEmergencyMode()
         ↓
Back to Normal Mode
```

---

*End of Complete Workflow Documentation*

