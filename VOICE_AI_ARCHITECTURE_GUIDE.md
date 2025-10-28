# Voice AI System Architecture & User Guide

> **Last Updated:** October 28, 2025  
> **For:** Developers and new team members  
> **Purpose:** Complete understanding of the Voice AI system from scratch

---

## 🎯 Executive Summary

The **Voice AI Assistant** is an emergency first aid guidance system that provides:
- **Voice-activated** emergency instructions
- **Real-time AI responses** via Gemini 2.0 Flash
- **Offline fallback mode** with pre-defined responses
- **Emergency quick actions** for critical situations
- **Text-to-Speech guidance** for hands-free operation

### **System Modes:**
1. **🟢 Online Mode:** AI-powered intelligent responses via Gemini API
2. **🔴 Offline Mode:** Pre-defined emergency responses without internet

---

## 📐 System Architecture

### **High-Level Architecture Diagram**

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                          │
│                  (VoiceAssistantFragment.kt)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │   Mic    │  │Emergency │  │  Status  │  │ Response │      │
│  │  Button  │  │ Buttons  │  │Indicator │  │  Display │      │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                             │
│                 (VoiceAssistantViewModel.kt)                    │
│  • State Management                                              │
│  • LiveData/StateFlow Observers                                  │
│  • UI Logic                                                      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                     MANAGER LAYER                                │
│                (VoiceAssistantManager.kt)                       │
│  • Orchestrates all components                                  │
│  • Workflow coordination                                        │
│  • State transitions                                            │
└──┬──────────────────┬──────────────────┬──────────────────┬────┘
   │                  │                  │                  │
   ▼                  ▼                  ▼                  ▼
┌────────┐     ┌────────────┐    ┌────────────┐    ┌──────────┐
│ Speech │     │  Gemini    │    │    TTS     │    │Permission│
│Recogni-│     │    AI      │    │  Manager   │    │ Manager  │
│ tion   │     │  Manager   │    │            │    │          │
└────────┘     └────────────┘    └────────────┘    └──────────┘
    │                │                  │                  │
    ▼                ▼                  ▼                  │
┌────────┐     ┌────────────┐    ┌────────────┐          │
│Android │     │  Gemini    │    │  Android   │          │
│Speech  │     │   API      │    │    TTS     │          │
│  API   │     │            │    │    API     │          │
└────────┘     └────────────┘    └────────────┘          │
    │                │                  │                  │
    └────────────────┴──────────────────┴──────────────────┘
                         │
                         ▼
                ┌────────────────┐
                │  Data Models   │
                │ VoiceCommand   │
                │ VoiceResponse  │
                │  VoiceAction   │
                └────────────────┘
```

---

## 🏗️ Component Breakdown

### **1. UI Layer (Frontend)**

#### **File:** `fragment_voice_assistant.xml`
**Responsibility:** Visual layout and UI elements

**Key Components:**
- **Normal Mode Layout** (Scrollable)
  - Microphone button (FAB)
  - AI status indicator (online/offline)
  - Status text display
  - State animations (listening/processing/speaking)
  - User input card (recognized text)
  - AI response card
  - Urgency indicator bar
  - Emergency quick action buttons (CPR, Choking, Bleeding, Burns)
  - Control buttons (Stop, Clear)

- **Emergency Overlay** (Full screen)
  - Emergency mode indicator
  - Step-by-step instructions
  - Progress indicator
  - Exit button

#### **File:** `VoiceAssistantFragment.kt`
**Responsibility:** UI logic and user interaction

**Key Methods:**
- `setupUI()` - Initialize button listeners
- `observeViewModel()` - React to state changes
- `updateUIForState()` - Update UI based on assistant state
- `updateAIStatusIndicator()` - Show online/offline status
- `handleMicrophoneClick()` - Start voice input
- `checkAndRequestPermissions()` - Permission handling

---

### **2. ViewModel Layer (Business Logic)**

#### **File:** `VoiceAssistantViewModel.kt`
**Responsibility:** State management and UI-Manager bridge

**LiveData Properties:**
```kotlin
val assistantState: LiveData<VoiceAssistantState>  // Current state
val currentResponse: LiveData<VoiceResponse>       // AI response
val recognizedText: LiveData<String>               // User's speech
val errorMessage: LiveData<String>                 // Error messages
val isInitialized: LiveData<Boolean>               // Init status
val isAIOnline: LiveData<Boolean>                  // AI availability
val showEmergencyMode: LiveData<Boolean>           // Emergency overlay
```

**Key Methods:**
- `initialize()` - Setup voice assistant
- `startListening()` - Begin voice input
- `handleEmergencyCommand()` - Process emergency actions
- `checkAIStatus()` - Verify AI availability
- `getOfflineEmergencyResponse()` - Fallback responses
- `startCPRGuidance()`, `startChokingGuidance()`, etc. - Quick emergency actions

---

### **3. Manager Layer (Orchestration)**

#### **File:** `VoiceAssistantManager.kt`
**Responsibility:** Central coordinator for all voice assistant functionality

**Component References:**
```kotlin
private val speechRecognition = SpeechRecognitionService(context)
private val textToSpeech = TextToSpeechManager(context)
private val geminiAI = GeminiAIManager(context)
private val permissionManager = VoicePermissionManager(context)
```

**State Management:**
```kotlin
sealed class VoiceAssistantState {
    object Idle              // Ready for input
    object Listening         // Recording voice
    object Processing        // Analyzing with AI
    object Speaking          // TTS output
    object Connecting        // Connecting to AI
    object LiveConversation  // Emergency mode active
    data class Error(message) // Error state
}
```

**Workflow Methods:**
- `initialize()` - Setup all components
- `startListening()` - Begin speech recognition
- `handleRecognizedSpeech()` - Process voice input
- `speakResponse()` - TTS output
- `handleEmergencyCommand()` - Emergency procedures
- `handleBasicCommand()` - Offline fallback

---

### **4. AI Component (Gemini Integration)**

#### **File:** `GeminiAIManager.kt`
**Responsibility:** AI processing and intelligent responses

**Configuration:**
```kotlin
Model: gemini-2.0-flash
Temperature: 0.7
Top-K: 40
Top-P: 0.95
```

**Key Features:**
- **Emergency-optimized prompt** for first aid scenarios
- **Urgency detection** (CRITICAL/HIGH/MEDIUM)
- **Action extraction** (call 911, navigate to procedure)
- **Offline fallback** with pre-defined responses

**Emergency Response Map:**
```kotlin
emergencyResponses = {
    "cpr"         → CPR instructions + Call 911
    "choking"     → Heimlich instructions
    "bleeding"    → Bleeding control steps
    "burns"       → Burn treatment steps
    "heart_attack"→ Heart attack response + Call 911
    "stroke"      → Stroke response + Call 911
    "fracture"    → Fracture care instructions
}
```

**Processing Flow:**
```
User Input
    ↓
Check AI Available?
    ├─ YES → processEmergencyVoiceInput()
    │           ↓
    │        Build emergency prompt
    │           ↓
    │        Call Gemini API
    │           ↓
    │        Parse response
    │           ↓
    │        Extract urgency & actions
    │           ↓
    │        Return VoiceResponse
    │
    └─ NO → processFallbackCommand()
                ↓
             Keyword matching
                ↓
             Return pre-defined response
```

---

### **5. Speech Recognition Component**

#### **File:** `SpeechRecognitionService.kt`
**Responsibility:** Convert speech to text

**Configuration:**
```kotlin
Language Model: FREE_FORM (natural speech)
Language: en-US
Partial Results: Enabled
Max Results: 5
Silence Timeout: 2000ms
Minimum Length: 1500ms
```

**Callbacks:**
- `onReadyForSpeech()` - Ready to listen
- `onBeginningOfSpeech()` - Speech detected
- `onPartialResults()` - Live transcription
- `onResults()` - Final recognized text
- `onError()` - Error handling with auto-retry

---

### **6. Text-to-Speech Component**

#### **File:** `TextToSpeechManager.kt`
**Responsibility:** Convert text to spoken audio

**Features:**
- **Android TTS API** integration
- **Configurable voice parameters** (speed, pitch)
- **Utterance callbacks** (start, done, error)
- **Queue management** (flush/add modes)
- **Retry mechanism** on initialization failure

**Default Settings:**
```kotlin
Language: en-US (fallback to system default)
Speech Rate: 1.0
Pitch: 1.0
```

---

### **7. Permission Management**

#### **File:** `VoicePermissionManager.kt`
**Responsibility:** Handle runtime permissions

**Required Permissions:**
1. `RECORD_AUDIO` - Voice input
2. `ACCESS_FINE_LOCATION` - Find hospitals
3. `ACCESS_COARSE_LOCATION` - Location services
4. `CALL_PHONE` - Emergency calls
5. `MODIFY_AUDIO_SETTINGS` - Audio control

**Methods:**
- `areAllPermissionsGranted()` - Check all
- `getMissingPermissions()` - Get denied list
- `getPermissionRationale()` - User-friendly explanation

---

## 🔄 Complete User Interaction Flow

### **Scenario 1: Voice Command (Online Mode)**

```
┌────────────────────────────────────────────────────────────┐
│ Step 1: User Taps Microphone Button                        │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ UI Layer (VoiceAssistantFragment)                          │
│ • microphoneButton.setOnClickListener triggered            │
│ • Calls handleMicrophoneClick()                            │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ ViewModel Layer (VoiceAssistantViewModel)                  │
│ • startListening() called                                  │
│ • Delegates to voiceAssistant.startListening()             │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Manager Layer (VoiceAssistantManager)                      │
│ • Validates initialization                                 │
│ • Stops any current TTS                                    │
│ • Sets state to LISTENING                                  │
│ • Calls speechRecognition.startListening()                 │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 2: Speech Recognition Active                          │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Speech Component (SpeechRecognitionService)                │
│ • Creates recognition intent                               │
│ • Sets up RecognitionListener callbacks                    │
│ • Starts Android Speech API                                │
│ • onReadyForSpeech() → UI shows "Listening..."            │
│ • onPartialResults() → Updates tvRecognizedText live       │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 3: User Speaks                                        │
│ User: "Someone is having chest pain"                       │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Speech Component                                            │
│ • onResults() callback triggered                           │
│ • Recognized: "Someone is having chest pain"               │
│ • Calls onResult callback                                  │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 4: AI Processing                                      │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Manager Layer                                               │
│ • handleRecognizedSpeech() called                          │
│ • Updates recognizedText LiveData                          │
│ • Sets state to PROCESSING                                 │
│ • Calls geminiAI.processVoiceCommand()                     │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ AI Component (GeminiAIManager)                             │
│ • Check if AI available (isModelInitialized)               │
│ • Build emergency prompt with system instructions          │
│ • Call Gemini API: gemini-2.0-flash                        │
│ • Receive AI response                                      │
│ • Parse response for:                                      │
│   ├─ Main text guidance                                    │
│   ├─ Action required (call 911)                            │
│   └─ Urgency level (CRITICAL)                              │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ AI Response Example:                                        │
│ "Have them sit down and rest immediately. Call 911 now.    │
│  Give aspirin if available and no allergies. Monitor       │
│  breathing and pulse while waiting for help."              │
│                                                             │
│ Metadata:                                                   │
│ • urgency: CRITICAL                                         │
│ • action: CallEmergency("911")                             │
│ • source: gemini_ai                                         │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 5: Response Handling                                  │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Manager Layer                                               │
│ • Receives Result.success(VoiceResponse)                   │
│ • Posts to currentResponse LiveData                        │
│ • Calls speakResponse(response.text)                       │
│ • Handles actionRequired if present                        │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 6: Text-to-Speech                                     │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ TTS Component (TextToSpeechManager)                        │
│ • Sets state to SPEAKING                                   │
│ • Calls Android TTS API                                    │
│ • onStart() → Log start                                    │
│ • Speaks: "Have them sit down and rest immediately..."     │
│ • onDone() → Sets state to IDLE                            │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 7: UI Updates                                         │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ UI Layer                                                    │
│ • tvRecognizedText shows user input                        │
│ • tvResponse shows AI guidance                             │
│ • urgencyIndicator turns RED (critical)                    │
│ • speakingAnimation visible                                │
│ • statusText shows "Speaking..."                           │
│ • aiStatusIndicator shows GREEN (online)                   │
└────────────────────────────────────────────────────────────┘
                   │
                   ▼
               [COMPLETE]
```

---

### **Scenario 2: Emergency Button (Offline Mode)**

```
┌────────────────────────────────────────────────────────────┐
│ Step 1: User Clicks "CPR" Emergency Button                 │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ UI Layer                                                    │
│ • btnEmergencyCpr.setOnClickListener triggered             │
│ • Calls viewModel.startCPRGuidance()                       │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ ViewModel Layer                                             │
│ • startCPRGuidance() called                                │
│ • Calls handleEmergencyCommand(EMERGENCY_CPR)              │
│ • Sets showEmergencyMode = true                            │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 2: Check AI Availability                              │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ AI Component                                                │
│ • isModelInitialized.get() → FALSE (No internet)           │
│ • Falls back to processFallbackCommand()                   │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 3: Offline Response                                   │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ AI Component                                                │
│ • Keyword match: "cpr"                                      │
│ • Returns emergencyResponses["cpr"]                        │
│ • Response:                                                 │
│   "Start CPR immediately. Place hands on center of         │
│    chest. Push hard and fast at least 2 inches deep,       │
│    100-120 compressions per minute. Call 911 now!"         │
│                                                             │
│ • actionRequired: CallEmergency("911")                     │
│ • metadata: urgency=HIGH, procedure=CPR                    │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 4: Speak Response                                     │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ TTS Component                                               │
│ • Speaks CPR instructions                                  │
│ • No internet required (TTS is local)                      │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ Step 5: Emergency Overlay UI                               │
└──────────────────┬─────────────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────────────┐
│ UI Layer                                                    │
│ • normalModeLayout.visibility = GONE                       │
│ • emergencyOverlay.visibility = VISIBLE                    │
│ • Shows full-screen emergency instructions                 │
│ • urgencyIndicator = RED                                   │
│ • aiStatusIndicator = RED (offline)                        │
│ • statusText = "AI: Offline Mode"                          │
└────────────────────────────────────────────────────────────┘
                   │
                   ▼
               [COMPLETE]
```

---

## 🌐 Online vs Offline Mode Comparison

| Feature | Online Mode (AI) | Offline Mode (Fallback) |
|---------|-----------------|-------------------------|
| **Internet Required** | ✅ Yes | ❌ No |
| **Response Type** | Dynamic AI-generated | Pre-defined templates |
| **Response Quality** | Context-aware, specific | Generic, keyword-based |
| **Urgency Detection** | Automatic from AI | Pre-set in responses |
| **Action Detection** | Intelligent extraction | Pre-defined actions |
| **Coverage** | Any emergency situation | 7 common emergencies |
| **Latency** | 2-3 seconds (API call) | Instant |
| **API Key Required** | ✅ Yes | ❌ No |
| **Status Indicator** | 🟢 Green | 🔴 Red |

### **Offline Mode Coverage:**
1. ✅ CPR / Cardiac arrest
2. ✅ Choking / Heimlich maneuver
3. ✅ Severe bleeding
4. ✅ Burns
5. ✅ Heart attack
6. ✅ Stroke
7. ✅ Fractures

---

## 🔧 Configuration & Setup

### **1. API Key Setup (for Online Mode)**

**File:** `local.properties`
```properties
GEMINI_API_KEY=your_actual_api_key_here
```

**File:** `app/build.gradle.kts`
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
    }
}
```

### **2. Dependencies**

**File:** `app/build.gradle.kts`
```kotlin
dependencies {
    // Gemini AI
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### **3. Permissions**

**File:** `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

---

## 🎨 UI State Reference

### **State Visual Indicators:**

| State | Status Text | Animation | Mic Button | AI Indicator |
|-------|------------|-----------|------------|--------------|
| **Idle** | "Tap to speak" | None | Enabled | Green/Red |
| **Listening** | "Listening..." | Listening | Active | Green/Red |
| **Processing** | "Processing..." | Processing | Disabled | Green/Red |
| **Speaking** | "Speaking..." | Speaking | Enabled | Green/Red |
| **Error** | "Error: {msg}" | None | Enabled | Red |
| **Emergency** | Emergency mode | Speaking | Enabled | Red |

### **Urgency Color Coding:**

| Urgency | Color | Hex Code |
|---------|-------|----------|
| **CRITICAL** | Red | `#CC0000` |
| **HIGH** | Orange | `#FF8800` |
| **MEDIUM** | Yellow | `#FFBB33` |
| **LOW** | Green | `#00C851` |

---

## 📱 User Guide

### **For First-Time Users:**

#### **1. Grant Permissions**
- When app opens, permission dialog appears
- Grant **all requested permissions** for full functionality
- Can still use with limited permissions (reduced features)

#### **2. Check AI Status**
- Look at AI status indicator (top of screen)
- 🟢 **Green = Online** → Full AI features
- 🔴 **Red = Offline** → Basic emergency responses

#### **3. Using Voice Commands**
1. Tap the **microphone button**
2. Wait for "Listening..." status
3. Speak clearly: "Someone is choking" or "CPR instructions"
4. AI processes and speaks response
5. Follow instructions

#### **4. Using Emergency Buttons**
1. Tap any emergency button (CPR, Choking, Bleeding, Burns)
2. Instant voice guidance starts
3. Emergency overlay shows step-by-step
4. Tap "Exit Emergency" when done

#### **5. Controls**
- **Stop Button:** Stops speaking/listening immediately
- **Clear Button:** Clears conversation, starts fresh

---

## 🐛 Troubleshooting

### **Common Issues:**

#### **"Voice assistant not available"**
**Cause:** Initialization failed  
**Solution:**
1. Check permissions granted
2. Restart app
3. Check TTS data installed

#### **"AI: Offline Mode" (unwanted)**
**Cause:** No internet or missing API key  
**Solution:**
1. Check internet connection
2. Verify `GEMINI_API_KEY` in `local.properties`
3. Rebuild app after adding API key

#### **"No speech recognized"**
**Cause:** Speech recognition timeout  
**Solution:**
- Auto-retries after 1 second
- Speak more clearly
- Check microphone permission

#### **TTS not speaking**
**Cause:** TTS not initialized  
**Solution:**
1. Check TTS data installed on device
2. Go to Settings → Language → Text-to-Speech
3. Download English (US) voice data

---

## 📊 Performance Metrics

### **Typical Response Times:**

| Stage | Online Mode | Offline Mode |
|-------|-------------|--------------|
| Speech Recognition | 2-3 seconds | 2-3 seconds |
| AI Processing | 2-4 seconds | <100ms |
| TTS Start | <500ms | <500ms |
| **Total Time** | **5-8 seconds** | **3-4 seconds** |

### **Resource Usage:**

- **Memory:** ~50-80 MB
- **Network:** ~5-10 KB per AI request
- **Battery:** Minimal impact (speech APIs are efficient)

---

## 🔐 Security & Privacy

### **Data Handling:**

1. **Voice Data:**
   - Processed locally by Android Speech API
   - Sent to Gemini API only for AI processing
   - Not stored on device or server

2. **API Key:**
   - Stored in `local.properties` (not in version control)
   - Included in BuildConfig at compile time
   - Never exposed to users

3. **Offline Mode:**
   - No data leaves device
   - Pre-defined responses only
   - Zero network usage

---

## 📚 File Structure Reference

```
app/src/main/
├── java/com/example/firstaidapp/
│   ├── ui/voice/
│   │   ├── VoiceAssistantFragment.kt      [UI Layer]
│   │   └── VoiceAssistantViewModel.kt     [ViewModel]
│   ├── voice/
│   │   ├── VoiceAssistantManager.kt       [Orchestrator]
│   │   ├── GeminiAIManager.kt             [AI Processing]
│   │   ├── SpeechRecognitionService.kt    [Speech Input]
│   │   ├── TextToSpeechManager.kt         [Speech Output]
│   │   └── VoicePermissionManager.kt      [Permissions]
│   └── data/voice/
│       ├── VoiceCommand.kt                 [Data Models]
│       └── EmergencyProcedure.kt           [Emergency Data]
└── res/
    └── layout/
        └── fragment_voice_assistant.xml    [UI Layout]
```

---

## 🎯 Quick Reference for Developers

### **Adding a New Emergency Response (Offline):**

**File:** `GeminiAIManager.kt`

```kotlin
private val emergencyResponses = mapOf(
    // ...existing responses...
    "snake_bite" to VoiceResponse(
        text = "Your emergency instructions here",
        actionRequired = VoiceAction.NavigateToProcedure("snake_bite"),
        metadata = mapOf("urgency" to "HIGH", "procedure" to "SNAKE_BITE")
    )
)
```

### **Customizing AI Prompt:**

**File:** `GeminiAIManager.kt`  
**Method:** `getEmergencyFirstAidPrompt()`

Modify the system instructions to change AI behavior.

### **Adding New UI State:**

1. Add to `VoiceAssistantState` sealed class
2. Handle in `updateUIForState()` in Fragment
3. Update state flow in Manager

---

## ✅ Testing Checklist

### **Before Release:**

- [ ] Permissions dialog shows and works
- [ ] Microphone button starts listening
- [ ] Speech recognition works
- [ ] AI responses appropriate (with internet)
- [ ] Offline mode works (without internet)
- [ ] All 4 emergency buttons work
- [ ] Emergency overlay displays correctly
- [ ] TTS speaks responses clearly
- [ ] Stop button stops TTS/listening
- [ ] Clear button resets conversation
- [ ] AI status indicator accurate
- [ ] Urgency colors display correctly
- [ ] State transitions smooth
- [ ] No crashes on rotation
- [ ] Error messages user-friendly

---

## 🎓 Learning Path for New Developers

### **Day 1: Understand the Flow**
1. Read this document completely
2. Read `VOICE_AI_COMPLETE_WORKFLOW.md`
3. Read `VOICE_AI_UI_MAPPING.md`

### **Day 2: Explore the Code**
1. Start with `VoiceAssistantFragment.kt`
2. Follow a button click through all layers
3. Add log statements to trace flow

### **Day 3: Test Modifications**
1. Change an offline response
2. Modify UI colors
3. Add a new emergency button

### **Day 4: Advanced Features**
1. Study AI prompt engineering
2. Understand state management
3. Review error handling

---

## 📞 Support & Contacts

For questions about this system, refer to:
- **Workflow:** `VOICE_AI_COMPLETE_WORKFLOW.md`
- **UI Mapping:** `VOICE_AI_UI_MAPPING.md`
- **General Voice AI:** `VOICE_AI_DOCUMENTATION.md`

---

*End of Voice AI System Architecture & User Guide*

**Version:** 1.0  
**Created:** October 28, 2025  
**Maintained by:** FirstAid App Development Team

