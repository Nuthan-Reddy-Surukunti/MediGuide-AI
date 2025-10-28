# Voice AI UI File Mapping Documentation

> **Last Updated:** October 28, 2025  
> **Purpose:** Complete mapping of Voice AI UI components and their corresponding files

---

## 📱 UI Component Hierarchy

### 1. Fragment Voice Assistant Layout
**File:** `app/src/main/res/layout/fragment_voice_assistant.xml`

#### Main Layout Structure
```xml
LinearLayout (Root)
├── ScrollView (Normal Mode) - id: normalModeLayout
│   └── LinearLayout (Content Container)
│       ├── TextView (Title) - "Voice Assistant"
│       ├── LinearLayout (AI Status Badge)
│       │   ├── View (Status Indicator) - id: aiStatusIndicator
│       │   └── TextView (Status Text) - id: aiStatusText
│       ├── TextView (Status Message) - id: statusText
│       ├── FrameLayout (Microphone Container)
│       │   ├── ProgressBar (Listening Animation) - id: listeningAnimation
│       │   ├── ProgressBar (Processing Animation) - id: processingAnimation
│       │   ├── ImageView (Speaking Animation) - id: speakingAnimation
│       │   └── FloatingActionButton (Mic Button) - id: microphoneButton
│       ├── View (Urgency Indicator) - id: urgencyIndicator
│       ├── MaterialCardView (User Input Card)
│       │   └── TextView (Recognized Text) - id: tvRecognizedText
│       ├── MaterialCardView (AI Response Card)
│       │   └── TextView (Response Text) - id: tvResponse
│       ├── TextView (Emergency Actions Title)
│       ├── GridLayout (Emergency Buttons - 2x2)
│       │   ├── MaterialButton (CPR) - id: btnEmergencyCpr
│       │   ├── MaterialButton (Choking) - id: btnEmergencyChoking
│       │   ├── MaterialButton (Bleeding) - id: btnEmergencyBleeding
│       │   └── MaterialButton (Burns) - id: btnEmergencyBurns
│       └── LinearLayout (Control Buttons)
│           ├── MaterialButton (Stop) - id: btnStop
│           └── MaterialButton (Clear) - id: btnClear
└── FrameLayout (Emergency Overlay) - id: emergencyOverlay
    └── LinearLayout (Emergency Content)
        ├── TextView (Emergency Mode Title)
        ├── TextView (Emergency Instructions) - id: tvEmergencyInstructions
        ├── ProgressBar (Step Progress) - id: stepProgress
        ├── TextView (Current Step) - id: tvCurrentStep
        └── MaterialButton (Exit Emergency) - id: btnExitEmergency
```

---

## 🎨 UI Element to Code Mapping

### AI Status Indicator (Online/Offline Status)

**UI Elements:**
- `aiStatusIndicator` - Colored dot (green/red)
- `aiStatusText` - Status text ("AI: Online" or "AI: Offline Mode")

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Method: updateAIStatusIndicator(isOnline: Boolean)
Lines: 202-218

Observes: viewModel.isAIOnline
Updates: Background color and text based on AI availability
```

---

### Microphone Button & State Animations

**UI Elements:**
- `microphoneButton` - Main FAB for voice input
- `listeningAnimation` - Shows when listening
- `processingAnimation` - Shows when processing
- `speakingAnimation` - Shows when speaking

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Method: updateUIForState(state: VoiceAssistantState)
Lines: 220-280

State Mapping:
├── Idle → All animations hidden, mic enabled
├── Listening → listeningAnimation visible
├── Processing → processingAnimation visible
├── Speaking → speakingAnimation visible
├── Connecting → processingAnimation visible, mic disabled
├── LiveConversation → listeningAnimation visible
└── Error → All animations hidden, error message shown
```

**Click Handler:**
```kotlin
File: VoiceAssistantFragment.kt
Method: setupUI() → microphoneButton.setOnClickListener
Lines: 68-70

Calls: handleMicrophoneClick() → viewModel.startListening()
```

---

### Status Text Display

**UI Element:**
- `statusText` - Shows current system status

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Method: updateUIForState(state: VoiceAssistantState)

Status Messages by State:
├── Idle → "Tap to speak"
├── Listening → "Listening..."
├── Processing → "Processing..."
├── Speaking → "Speaking..."
├── Connecting → "Connecting to Live AI..."
├── LiveConversation → "🚨 LIVE Emergency Assistant Active"
└── Error → "Error: {message}"
```

---

### Recognized Text Display

**UI Element:**
- `tvRecognizedText` - Shows what user said

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Observer: viewModel.recognizedText.observe
Lines: 115-117

Updates when:
├── Partial speech results received
├── Final speech recognition complete
└── Manual text input (if implemented)
```

---

### AI Response Display

**UI Element:**
- `tvResponse` - Shows AI's response
- `urgencyIndicator` - Color bar showing urgency level

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Observer: viewModel.currentResponse.observe
Lines: 120-145

Response Processing:
├── Displays response.text
├── Extracts urgency from metadata
└── Updates urgencyIndicator color:
    ├── HIGH → Red (holo_red_dark)
    ├── MEDIUM → Orange (holo_orange_dark)
    └── Default → Green (holo_green_dark)
```

---

### Emergency Quick Action Buttons

**UI Elements:**
- `btnEmergencyCpr` - CPR emergency
- `btnEmergencyChoking` - Choking emergency
- `btnEmergencyBleeding` - Bleeding emergency
- `btnEmergencyBurns` - Burns emergency

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Method: setupUI()
Lines: 72-87

Button Mapping:
├── btnEmergencyCpr → viewModel.startCPRGuidance()
├── btnEmergencyChoking → viewModel.startChokingGuidance()
├── btnEmergencyBleeding → viewModel.startBleedingGuidance()
└── btnEmergencyBurns → viewModel.startBurnsGuidance()

Each calls:
└── viewModel.handleEmergencyCommand(commandType)
    └── Shows emergency overlay
    └── Starts voice guidance
```

---

### Control Buttons

**UI Elements:**
- `btnStop` - Stop speaking/listening
- `btnClear` - Clear conversation

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt

btnStop.setOnClickListener:
├── viewModel.stopSpeaking()
└── viewModel.stopListening()

btnClear.setOnClickListener:
├── viewModel.clearConversation()
├── Clear UI text displays
└── Show confirmation toast
```

---

### Emergency Overlay Mode

**UI Elements:**
- `emergencyOverlay` - Full-screen red overlay
- `tvEmergencyInstructions` - Emergency procedure text
- `stepProgress` - Progress through steps
- `tvCurrentStep` - Current step display
- `btnExitEmergency` - Exit emergency mode

**Connected Code:**
```kotlin
File: VoiceAssistantFragment.kt
Observer: viewModel.showEmergencyMode.observe
Lines: 167-175

Toggle Logic:
├── showEmergency = true:
│   ├── emergencyOverlay.visibility = VISIBLE
│   └── normalModeLayout.visibility = GONE
└── showEmergency = false:
    ├── emergencyOverlay.visibility = GONE
    └── normalModeLayout.visibility = VISIBLE

btnExitEmergency → viewModel.exitEmergencyMode()
```

---

## 🔄 Recent Layout Changes (October 2025)

### Added Features:
1. **AI Status Indicator**
   - Visual online/offline indicator
   - Real-time connection status
   - Fallback to offline mode gracefully

2. **Emergency Overlay Mode**
   - Full-screen emergency guidance
   - Step-by-step progress tracking
   - Dedicated exit button

3. **Urgency Color Coding**
   - Visual urgency indicator bar
   - Color-coded response cards
   - High-contrast emergency colors

4. **Enhanced State Animations**
   - Separate animations for each state
   - Smooth transitions
   - Visual feedback for user

### Modified Components:
1. **Status Text** - Now shows more detailed states
2. **Microphone Button** - Disabled during certain states
3. **Response Cards** - Color-coded by urgency
4. **Emergency Buttons** - Material Design 3 styling

---

## 📊 UI State Flow Diagram

```
User Interaction Flow:
┌─────────────────────────────────────────────────┐
│ 1. User Opens Voice Assistant Fragment          │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│ 2. Fragment Created                              │
│    - Binding inflated                            │
│    - setupUI() called                            │
│    - observeViewModel() called                   │
│    - checkAndRequestPermissions() called         │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│ 3. Permissions Check                             │
│    - Shows permission dialog if needed           │
│    - Initializes voice assistant when granted    │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│ 4. Voice Assistant Ready (Idle State)           │
│    - Microphone button enabled                   │
│    - Status: "Tap to speak"                      │
│    - AI status indicator updated                 │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┴──────────────┐
    │                           │
    ▼                           ▼
[Mic Clicked]           [Emergency Button Clicked]
```

---

## 🎯 View ID Quick Reference

| View ID | Type | Purpose | Visibility Control |
|---------|------|---------|-------------------|
| `normalModeLayout` | ScrollView | Main scrollable content | Toggles with emergency overlay |
| `emergencyOverlay` | FrameLayout | Full-screen emergency mode | Toggles with normal mode |
| `aiStatusIndicator` | View | AI connection status dot | Always visible |
| `aiStatusText` | TextView | AI status label | Always visible |
| `statusText` | TextView | Current state message | Always visible |
| `microphoneButton` | FloatingActionButton | Voice input trigger | Always visible, enabled/disabled |
| `listeningAnimation` | ProgressBar | Listening indicator | Visible during Listening state |
| `processingAnimation` | ProgressBar | Processing indicator | Visible during Processing state |
| `speakingAnimation` | ImageView | Speaking indicator | Visible during Speaking state |
| `urgencyIndicator` | View | Urgency color bar | Always visible, color changes |
| `tvRecognizedText` | TextView | User's speech | Always visible |
| `tvResponse` | TextView | AI response | Always visible |
| `btnEmergencyCpr` | MaterialButton | CPR quick action | Always enabled |
| `btnEmergencyChoking` | MaterialButton | Choking quick action | Always enabled |
| `btnEmergencyBleeding` | MaterialButton | Bleeding quick action | Always enabled |
| `btnEmergencyBurns` | MaterialButton | Burns quick action | Always enabled |
| `btnStop` | MaterialButton | Stop TTS/listening | Always enabled |
| `btnClear` | MaterialButton | Clear conversation | Always enabled |
| `btnExitEmergency` | MaterialButton | Exit emergency overlay | Visible in emergency mode |

---

## 📝 Notes for Developers

### Key Points:
1. **All UI updates happen via ViewModel observers** - Never update UI directly
2. **State-driven animations** - Animations controlled by VoiceAssistantState
3. **Graceful degradation** - UI works even if AI is offline
4. **Emergency-first design** - Emergency actions always accessible
5. **Accessibility** - All elements have content descriptions (to be added)

### Best Practices:
- Use ViewBinding for type-safe view access
- Observe LiveData in viewLifecycleOwner scope
- Handle null states gracefully
- Show user feedback for all actions
- Maintain consistent visual hierarchy

---

*End of UI Mapping Documentation*

