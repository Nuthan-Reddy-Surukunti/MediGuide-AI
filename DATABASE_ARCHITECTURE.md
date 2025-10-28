# Database Architecture & Data Storage — FirstAidApp

Last updated: October 28, 2025

This document explains the complete database and data storage architecture of the FirstAidApp, including initialization workflow, data sources, storage mechanisms, and file structure.

---

## Quick Summary

- **No traditional database (Room/SQLite)** — The app uses a hybrid approach with in-memory data and SharedPreferences
- **Primary storage mechanisms:**
  1. **Kotlin hardcoded data objects** (FirstAidGuidesData, FirstAidGuidesRepository, EmergencyContactsData)
  2. **SharedPreferences** (user preferences, favorites, contacts, search history, medical info, profile)
  3. **JSON fallback support** (guides.json, emergency_contacts.json in assets — currently empty, fallback to Kotlin data)
  4. **Firebase Authentication** (user login state only, no Firestore database)

- **Data persistence:** User-generated data (favorites, contacts, settings, profile) is stored in SharedPreferences; app data (guides, steps) is loaded from Kotlin objects on app start.

---

## 1. Database Architecture Overview

### Architecture Type
- **Hybrid In-Memory + SharedPreferences Architecture**
- No Room database or SQLite database
- No Firebase Firestore/Realtime Database (only Firebase Auth for login)
- Data is loaded into memory from Kotlin objects and managed via Repository/Manager pattern

### Why this approach?
- First aid guides are **static, read-only content** — no need for a database
- User preferences and small datasets fit well in SharedPreferences
- Fast startup and no database migration overhead
- Simplified architecture for a medium-sized app

---

## 2. Data Storage Layers

### Layer 1: Hardcoded Data Sources (Kotlin Objects)

These provide the core first aid guide content, loaded into memory at runtime.

#### File: `FirstAidGuidesData.kt`
- **Location:** `app/src/main/java/com/example/firstaidapp/data/repository/FirstAidGuidesData.kt`
- **Purpose:** Defines all FirstAidGuide objects with metadata (title, category, severity, description, warnings, YouTube links)
- **Data:** ~20+ guides including CPR, Choking, Heart Attack, Stroke, Bleeding, Burns, Fractures, etc.
- **Load time:** On-demand when JsonGuideManager initializes

```kotlin
object FirstAidGuidesData {
    fun getAllFirstAidGuides(): List<FirstAidGuide> {
        return listOf(
            FirstAidGuide(
                id = "cpr_guide",
                title = "CPR - Adult",
                category = "CPR",
                severity = "CRITICAL",
                description = "Cardiopulmonary resuscitation...",
                steps = FirstAidGuidesRepository.getCPRGuide(),
                // ... more fields
            ),
            // ... more guides
        )
    }
}
```

#### File: `FirstAidGuidesRepository.kt`
- **Location:** `app/src/main/java/com/example/firstaidapp/data/repository/FirstAidGuidesRepository.kt`
- **Purpose:** Provides detailed step-by-step instructions for each guide
- **Data:** GuideStep objects with stepNumber, title, description, detailedInstructions, tips, warnings, images, duration, tools required
- **Methods:**
  - `getCPRGuide()` — returns List<GuideStep> for CPR
  - `getChokingAdultGuide()` — choking steps
  - `getHeartAttackGuide()` — heart attack steps
  - `getSevereBleedingGuide()` — bleeding control steps
  - ... (one method per guide)

```kotlin
object FirstAidGuidesRepository {
    fun getCPRGuide(): List<GuideStep> {
        return listOf(
            GuideStep(
                id = "cpr_step_1",
                stepNumber = 1,
                title = "Check for Responsiveness",
                description = "Gently tap the person's shoulders...",
                detailedInstructions = "Place your hands on...",
                iconRes = R.drawable.ic_visibility,
                imageRes = R.drawable.cpr_check_responsiveness,
                duration = "10 seconds",
                stepType = StepType.CHECK,
                isCritical = true,
                tips = listOf("Tap firmly but not aggressively", ...),
                warnings = listOf("Do not shake if you suspect spinal injury")
            ),
            // ... more steps
        )
    }
}
```

#### File: `EmergencyContactsData.kt`
- **Location:** `app/src/main/java/com/example/firstaidapp/data/repository/EmergencyContactsData.kt`
- **Purpose:** Provides default emergency contacts for all Indian states (Police, Fire, Ambulance, Disaster, Women's Helpline, etc.)
- **Data:** EmergencyContact objects with name, phone number, type, state, description
- **Methods:**
  - `getAllEmergencyContactsWithStates()` — returns List<EmergencyContact> for all states
  - Includes national contacts (112, 100, 101, 102, 108, 1091, 1098, etc.)

---

### Layer 2: SharedPreferences (User Data & Preferences)

All user-generated and user-specific data is stored in SharedPreferences XML files.

#### Primary SharedPreferences Files

1. **`first_aid_prefs`** — Managed by `PreferencesManager.kt`
   - Favorites (guide IDs)
   - View counts per guide
   - Last accessed timestamps per guide
   - Search history (JSON array of strings)
   - User-added emergency contacts (JSON array of EmergencyContact)
   - Next contact ID counter

2. **`FirstAidUserPrefs`** — Managed by `UserPreferencesManager.kt`
   - Theme settings (dark mode, theme mode)
   - App settings (sound, vibration, emergency confirmation)
   - Guide preferences (font size, show images, auto scroll)
   - Search preferences
   - Privacy settings (analytics, crash reporting)
   - Emergency preferences
   - Onboarding status
   - State selection for contacts
   - Contacts permission tracking
   - Learning reminders
   - User profile (name, bio, profile image URI, date joined)
   - Medical information (blood type, allergies, medications, conditions, doctor info)

#### PreferencesManager.kt Details
- **Location:** `app/src/main/java/com/example/firstaidapp/managers/PreferencesManager.kt`
- **Storage:** Uses Gson to serialize/deserialize complex objects (List<EmergencyContact>, List<String>)
- **Key methods:**
  - `addFavorite(guideId)` / `removeFavorite(guideId)` / `getFavorites()` / `isFavorite(guideId)`
  - `incrementViewCount(guideId)` / `getViewCount(guideId)`
  - `updateLastAccessed(guideId)` / `getLastAccessed(guideId)`
  - `addSearchQuery(query)` / `getSearchHistory()` / `clearSearchHistory()`
  - `addUserContact(contact)` / `updateUserContact(contact)` / `deleteUserContact(contactId)` / `getUserContacts()`
  - `getNextContactId()` — auto-increment for user contacts
  - `isDataInitialized()` / `setDataInitialized(initialized)` — tracks first run

#### UserPreferencesManager.kt Details
- **Location:** `app/src/main/java/com/example/firstaidapp/utils/UserPreferencesManager.kt`
- **Storage:** Simple key-value pairs (String, Boolean, Int, Long)
- **Key properties (all backed by SharedPreferences):**
  - `isDarkModeEnabled: Boolean`
  - `themeMode: String` (system/light/dark)
  - `isSoundEnabled: Boolean`
  - `isVibrationEnabled: Boolean`
  - `requireEmergencyConfirmation: Boolean`
  - `selectedState: String` (for emergency contacts filtering)
  - `isStateSelectionDone: Boolean`
  - `userName: String`
  - `userBio: String`
  - `profileImageUri: String`
  - `bloodType: String`
  - `allergies: String`
  - `medications: String`
  - `medicalConditions: String`
  - `emergencyNotes: String`
  - `doctorName: String`
  - `doctorContact: String`
  - ... and many more

---

### Layer 3: JSON Assets (Fallback, Currently Unused)

The app supports loading data from JSON files in `app/src/main/assets/`, but these files are currently empty (`[]`). The app falls back to Kotlin data sources.

#### File: `guides.json`
- **Location:** `app/src/main/assets/guides.json`
- **Current content:** `[]` (empty)
- **Purpose:** Could store FirstAidGuide objects in JSON format
- **Fallback:** If empty or invalid, `JsonGuideManager` loads from `FirstAidGuidesData.getAllFirstAidGuides()`

#### File: `emergency_contacts.json`
- **Location:** `app/src/main/assets/emergency_contacts.json`
- **Current content:** `[]` (empty)
- **Purpose:** Could store EmergencyContact objects in JSON format
- **Fallback:** If empty or invalid, `ContactManager` loads from `EmergencyContactsData.getAllEmergencyContactsWithStates()`

---

### Layer 4: Firebase Authentication (Login State Only)

#### Firebase Auth
- **Used for:** User login/logout state only (Google Sign-In, email/password)
- **NOT used for:** Storing user data, guides, contacts, preferences
- **Files:**
  - `LoginFragment.kt` — handles Firebase auth with Google Sign-In
  - `SignUpFragment.kt` — handles Firebase email/password signup
  - `ProfileFragment.kt` — handles Firebase sign-out
  - `MainActivity.kt` — checks if user is logged in

#### What Firebase stores:
- User UID (unique ID)
- Email address
- Display name (from Google)
- Profile photo URL (from Google)

#### What Firebase does NOT store:
- User preferences (stored in SharedPreferences)
- Medical info (stored in SharedPreferences)
- Favorites, contacts, search history (stored in SharedPreferences)
- First aid guides (hardcoded in Kotlin)

---

## 3. Data Initialization Workflow

### App Startup Sequence

1. **Application.onCreate()** — `FirstAidApplication.kt`
   - Minimal initialization (no heavy work)
   - Comment: "Managers initialize on-demand when first ViewModels are created"

2. **Activity.onCreate()** — `MainActivity.kt`
   - Check Firebase auth state (`FirebaseAuth.getInstance().currentUser`)
   - If not logged in → navigate to LoginFragment
   - If logged in → show home screen

3. **ViewModel initialization** (on-demand, when Fragment is created)
   - Example: `AllGuidesViewModel` → creates `JsonGuideManager(context)`
   - `JsonGuideManager.init{}` → calls `loadGuides()`

4. **JsonGuideManager.loadGuides()** workflow:
   ```
   Try:
     1. Open assets/guides.json
     2. Parse JSON → List<FirstAidGuide>
     3. If valid and non-empty → use JSON data
     4. If empty/invalid → fallback to FirstAidGuidesData.getAllFirstAidGuides()
   Catch:
     5. On error → fallback to FirstAidGuidesData.getAllFirstAidGuides()
   Finally:
     6. Post to LiveData (_guidesLiveData)
     7. Update categories and favorites LiveData
   ```

5. **ContactManager.loadContacts()** workflow (similar):
   ```
   Try:
     1. Open assets/emergency_contacts.json
     2. Parse JSON → List<EmergencyContact>
     3. If valid and non-empty → use JSON data
     4. If empty/invalid → fallback to EmergencyContactsData.getAllEmergencyContactsWithStates()
   Catch:
     5. On error → fallback to EmergencyContactsData
   Finally:
     6. Merge with user-added contacts from PreferencesManager
     7. Post to Flow (_contactsFlow)
   ```

6. **PreferencesManager initialization** (lazy, on first access):
   - No heavy init needed
   - SharedPreferences loaded from XML on first read

---

## 4. Manager Classes (Data Access Layer)

### JsonGuideManager.kt
- **Location:** `app/src/main/java/com/example/firstaidapp/managers/JsonGuideManager.kt`
- **Purpose:** Centralized manager for all first aid guides
- **Data source:** JSON (fallback to Kotlin data)
- **Caching:** Loads once in `init{}`, keeps in memory (`allGuides: List<FirstAidGuide>`)
- **Key methods:**
  - `getAllGuides(): LiveData<List<FirstAidGuide>>`
  - `getGuideById(guideId): LiveData<FirstAidGuide?>`
  - `searchGuides(query): LiveData<List<FirstAidGuide>>`
  - `getGuidesByCategory(category): LiveData<List<FirstAidGuide>>`
  - `getFavoriteGuides(): LiveData<List<FirstAidGuide>>`
  - `getRecentlyViewedGuides(): LiveData<List<FirstAidGuide>>`
  - `getMostViewedGuides(): LiveData<List<FirstAidGuide>>`

### ContactManager.kt
- **Location:** `app/src/main/java/com/example/firstaidapp/managers/ContactManager.kt`
- **Purpose:** Manages emergency contacts (default + user-added)
- **Data source:** JSON (fallback to Kotlin data) + SharedPreferences (user contacts)
- **Caching:** Loads once, keeps in StateFlow (`_contactsFlow`)
- **Key methods:**
  - `getAllContacts(): Flow<List<EmergencyContact>>`
  - `getContactsByState(state): Flow<List<EmergencyContact>>`
  - `getContactsByType(type): Flow<List<EmergencyContact>>`
  - `addUserContact(contact)`
  - `updateUserContact(contact)`
  - `deleteUserContact(contactId)`
  - `refreshContacts()` — reload from sources

### PreferencesManager.kt
- **Location:** `app/src/main/java/com/example/firstaidapp/managers/PreferencesManager.kt`
- **Purpose:** Manages all user-generated data in SharedPreferences
- **Storage:** `first_aid_prefs` SharedPreferences file
- **Uses Gson for serialization** (contacts, search history)

### UserPreferencesManager.kt
- **Location:** `app/src/main/java/com/example/firstaidapp/utils/UserPreferencesManager.kt`
- **Purpose:** Manages user settings and profile data
- **Storage:** `FirstAidUserPrefs` SharedPreferences file
- **Simple key-value pairs** (no Gson needed)

---

## 5. Data Models

### Core Models (Data Classes)

#### FirstAidGuide
- **File:** `app/src/main/java/com/example/firstaidapp/data/models/FirstAidGuide.kt`
- **Fields:**
  - `id: String` (unique identifier, e.g., "cpr_guide")
  - `title: String`
  - `category: String` (e.g., "CPR", "Airway", "Trauma")
  - `severity: String` (e.g., "CRITICAL", "HIGH", "MEDIUM")
  - `description: String`
  - `steps: List<GuideStep>` (detailed step-by-step instructions)
  - `iconResName: String?` (icon resource name)
  - `whenToCallEmergency: String?`
  - `warnings: List<String>`
  - `estimatedTimeMinutes: Int`
  - `difficulty: String` (e.g., "Beginner", "Intermediate")
  - `youtubeLink: String?`
  - `isFavorite: Boolean` (runtime flag, not persisted in object)
  - `lastAccessedTimestamp: Long` (runtime, not persisted)
  - `viewCount: Int` (runtime, not persisted)

#### GuideStep
- **File:** `app/src/main/java/com/example/firstaidapp/data/models/GuideStep.kt`
- **Fields:**
  - `id: String`
  - `guideId: String` (parent guide)
  - `stepNumber: Int`
  - `title: String`
  - `description: String` (short summary)
  - `detailedInstructions: String` (full instructions)
  - `iconRes: Int` (drawable resource ID)
  - `imageRes: Int?` (step illustration drawable)
  - `duration: String?` (e.g., "10 seconds")
  - `stepType: StepType` (CHECK, ACTION, CALL, WARNING)
  - `isCritical: Boolean`
  - `tips: List<String>`
  - `warnings: List<String>`
  - `requiredTools: List<String>?` (e.g., "AED", "Clean cloth")

#### EmergencyContact
- **File:** `app/src/main/java/com/example/firstaidapp/data/models/EmergencyContact.kt`
- **Fields:**
  - `id: Long` (auto-increment for user contacts)
  - `name: String`
  - `phoneNumber: String`
  - `type: ContactType` (POLICE, FIRE, AMBULANCE, DISASTER, HELPLINE, etc.)
  - `state: String` (e.g., "Karnataka", "National")
  - `isDefault: Boolean` (true = system-provided, false = user-added)
  - `description: String?`
  - `relationship: String?` (for personal contacts)
  - `notes: String?`
  - `isActive: Boolean`

#### UserProfile
- **File:** `app/src/main/java/com/example/firstaidapp/data/models/UserProfile.kt`
- **Fields:**
  - `name: String`
  - `bio: String`
  - `profileImageUri: String`
  - `dateJoined: Long`

#### MedicalInfo
- **File:** `app/src/main/java/com/example/firstaidapp/data/models/MedicalInfo.kt`
- **Fields:**
  - `bloodType: String`
  - `allergies: String`
  - `medications: String`
  - `medicalConditions: String`
  - `emergencyNotes: String`
  - `doctorName: String`
  - `doctorContact: String`

---

## 6. Initialization & Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         App Startup                              │
├─────────────────────────────────────────────────────────────────┤
│  1. FirstAidApplication.onCreate()                               │
│     └─> Minimal init, managers created on-demand                 │
│                                                                   │
│  2. MainActivity.onCreate()                                      │
│     └─> Check Firebase auth state                                │
│         ├─> Logged in? → Show home                               │
│         └─> Not logged in? → Show login                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Fragment/ViewModel Created                      │
├─────────────────────────────────────────────────────────────────┤
│  3. AllGuidesFragment → AllGuidesViewModel                       │
│     └─> ViewModel creates JsonGuideManager(context)             │
│                                                                   │
│  4. JsonGuideManager.init{}                                      │
│     └─> loadGuides()                                             │
│         ├─> Try: Load from assets/guides.json                    │
│         │   ├─> Success & valid? Use JSON data                   │
│         │   └─> Empty/invalid? Fallback to Kotlin data           │
│         └─> Catch: Fallback to FirstAidGuidesData               │
│                                                                   │
│  5. Data loaded into memory (allGuides: List<FirstAidGuide>)    │
│     └─> Post to LiveData for UI observation                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  User Interactions (Runtime)                     │
├─────────────────────────────────────────────────────────────────┤
│  • View guide → PreferencesManager.incrementViewCount(guideId)  │
│                 PreferencesManager.updateLastAccessed(guideId)   │
│  • Favorite → PreferencesManager.addFavorite(guideId)            │
│  • Search → PreferencesManager.addSearchQuery(query)             │
│  • Add contact → PreferencesManager.addUserContact(contact)      │
│  • Update profile → UserPreferencesManager.userName = "..."      │
│  • Change theme → UserPreferencesManager.themeMode = "dark"      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Data Persisted in Storage                      │
├─────────────────────────────────────────────────────────────────┤
│  SharedPreferences XML files (device internal storage):          │
│  • /data/data/com.example.firstaidapp/shared_prefs/             │
│    ├─> first_aid_prefs.xml (favorites, contacts, history)       │
│    └─> FirstAidUserPrefs.xml (settings, profile, medical)       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Where Data is Stored (Summary Table)

| Data Type | Storage Mechanism | Location | Managed By |
|-----------|-------------------|----------|------------|
| First aid guides (content) | Kotlin objects (in-memory) | `FirstAidGuidesData.kt` | `JsonGuideManager.kt` |
| Guide steps (detailed) | Kotlin objects (in-memory) | `FirstAidGuidesRepository.kt` | `JsonGuideManager.kt` |
| Default emergency contacts | Kotlin objects (in-memory) | `EmergencyContactsData.kt` | `ContactManager.kt` |
| User favorites | SharedPreferences | `first_aid_prefs.xml` | `PreferencesManager.kt` |
| View counts | SharedPreferences | `first_aid_prefs.xml` | `PreferencesManager.kt` |
| Last accessed times | SharedPreferences | `first_aid_prefs.xml` | `PreferencesManager.kt` |
| Search history | SharedPreferences (JSON) | `first_aid_prefs.xml` | `PreferencesManager.kt` |
| User-added contacts | SharedPreferences (JSON) | `first_aid_prefs.xml` | `PreferencesManager.kt` |
| User profile | SharedPreferences | `FirstAidUserPrefs.xml` | `UserPreferencesManager.kt` |
| Medical info | SharedPreferences | `FirstAidUserPrefs.xml` | `UserPreferencesManager.kt` |
| App settings | SharedPreferences | `FirstAidUserPrefs.xml` | `UserPreferencesManager.kt` |
| Theme preferences | SharedPreferences | `FirstAidUserPrefs.xml` | `UserPreferencesManager.kt` |
| Login state | Firebase Auth | Firebase servers | Firebase SDK |
| JSON fallback (guides) | Assets (empty) | `app/src/main/assets/guides.json` | `JsonGuideManager.kt` |
| JSON fallback (contacts) | Assets (empty) | `app/src/main/assets/emergency_contacts.json` | `ContactManager.kt` |

---

## 8. File Structure (Data Layer)

```
app/src/main/java/com/example/firstaidapp/
│
├── data/
│   ├── models/
│   │   ├── FirstAidGuide.kt          [Guide metadata model]
│   │   ├── GuideStep.kt               [Step-by-step instruction model]
│   │   ├── EmergencyContact.kt        [Contact model]
│   │   ├── UserProfile.kt             [User profile model]
│   │   ├── MedicalInfo.kt             [Medical info model]
│   │   ├── ContactType.kt             [Enum for contact types]
│   │   ├── StepType.kt                [Enum for step types]
│   │   ├── SearchHistory.kt           [Search history model]
│   │   └── PhoneContact.kt            [Phone contact model]
│   │
│   ├── repository/
│   │   ├── FirstAidGuidesData.kt      [Hardcoded guides list]
│   │   ├── FirstAidGuidesRepository.kt [Hardcoded guide steps]
│   │   └── EmergencyContactsData.kt   [Hardcoded emergency contacts]
│   │
│   └── voice/
│       ├── VoiceCommand.kt            [Voice command models]
│       └── EmergencyProcedure.kt      [Emergency procedure models]
│
├── managers/
│   ├── JsonGuideManager.kt            [Guide data manager]
│   ├── ContactManager.kt              [Contact data manager]
│   └── PreferencesManager.kt          [User data persistence]
│
├── utils/
│   ├── UserPreferencesManager.kt      [User settings & profile]
│   └── LearningNotificationManager.kt [Notification scheduling]
│
└── FirstAidApplication.kt             [Application entry point]
```

---

## 9. Key Differences from Traditional Database Apps

| Traditional DB App (Room/SQLite) | This App (Hybrid) |
|----------------------------------|-------------------|
| Schema migrations required | No migrations — data is hardcoded |
| DAO + Entity + Database classes | Manager + Data object pattern |
| SQL queries | Kotlin filtering/mapping |
| Async database I/O | Synchronous in-memory access |
| Data stored in `.db` file | Data in Kotlin code + SharedPreferences XML |
| Complex setup | Simple initialization |
| Good for dynamic data | Good for static content + small user data |

---

## 10. Testing & Debugging Data Flow

### How to check what data is loaded
1. **Guides:** Check `JsonGuideManager` logs:
   ```
   Tag: "JsonGuideManager"
   Log: "Loaded X guides from JSON" or "Loaded X guides from Kotlin data"
   ```

2. **Contacts:** Check `ContactManager` logs:
   ```
   Tag: "ContactManager"
   Log: "Loaded X contacts from JSON" or "Fallback to Kotlin data source"
   ```

3. **SharedPreferences:** Inspect device files (requires root or backup):
   ```
   /data/data/com.example.firstaidapp/shared_prefs/
   ├── first_aid_prefs.xml
   └── FirstAidUserPrefs.xml
   ```

4. **Firebase Auth:** Check FirebaseAuth current user:
   ```kotlin
   val user = FirebaseAuth.getInstance().currentUser
   Log.d("Auth", "User: ${user?.email}, UID: ${user?.uid}")
   ```

### How to reset data
- **Guides/Contacts:** Uninstall/reinstall app (data reloads from Kotlin objects)
- **User preferences:** Call `PreferencesManager.clearAllData()` or `UserPreferencesManager` clear methods
- **Login state:** Call `FirebaseAuth.getInstance().signOut()`

---

## 11. Adding New Data (Developer Guide)

### Adding a new First Aid Guide

1. **Add guide metadata** in `FirstAidGuidesData.kt`:
   ```kotlin
   FirstAidGuide(
       id = "new_guide",
       title = "New Procedure",
       category = "Trauma",
       severity = "HIGH",
       description = "...",
       steps = FirstAidGuidesRepository.getNewGuideSteps(),
       // ... other fields
   )
   ```

2. **Add guide steps** in `FirstAidGuidesRepository.kt`:
   ```kotlin
   fun getNewGuideSteps(): List<GuideStep> {
       return listOf(
           GuideStep(
               id = "new_step_1",
               guideId = "new_guide",
               stepNumber = 1,
               title = "Step Title",
               // ... fields
           ),
           // ... more steps
       )
   }
   ```

3. **No database migration needed** — data is loaded automatically on next app start.

### Adding a new Emergency Contact

1. **Add to default contacts** in `EmergencyContactsData.kt`:
   ```kotlin
   EmergencyContact(
       id = 0,
       name = "New Helpline",
       phoneNumber = "1234",
       type = ContactType.HELPLINE,
       state = "National",
       isDefault = true,
       description = "..."
   )
   ```

2. **Or let user add** via UI → saved in SharedPreferences automatically.

---

## 12. Future Migration to Room Database (Optional)

If the app grows and requires a full database:

### Migration Plan
1. **Add Room dependencies** in `build.gradle.kts`:
   ```kotlin
   implementation("androidx.room:room-runtime:2.6.0")
   ksp("androidx.room:room-compiler:2.6.0")
   implementation("androidx.room:room-ktx:2.6.0")
   ```

2. **Create Entity classes** (annotate existing models with `@Entity`, `@PrimaryKey`)

3. **Create DAO interfaces** (replace Manager classes with DAO queries)

4. **Create Database class** (extend RoomDatabase)

5. **Migrate SharedPreferences data** to Room on first launch

6. **Update ViewModels** to use DAOs instead of Managers

7. **Keep backward compatibility** with fallback to current system

---

## 13. Summary & Best Practices

### Current Architecture Strengths
✅ Fast startup (no database initialization)  
✅ Simple codebase (no complex queries)  
✅ Easy to understand and maintain  
✅ Good for static content + small user data  
✅ No migration headaches  

### Current Architecture Limitations
⚠️ Not suitable for large dynamic datasets  
⚠️ Limited offline sync capabilities  
⚠️ SharedPreferences has size limits (~1-2 MB recommended)  
⚠️ No relational queries (manual filtering required)  

### When to Migrate to Room
- If guides become user-editable or dynamic
- If user data exceeds ~100 contacts or complex relationships
- If you need advanced search/filtering with indexes
- If you add collaborative features or cloud sync

### Best Practices (Current Architecture)
1. Keep guides/contacts in Kotlin objects for now (fast, simple)
2. Use SharedPreferences only for small user data
3. Use Gson for complex objects in SharedPreferences
4. Log data loading for debugging (`JsonGuideManager`, `ContactManager`)
5. Provide fallback to Kotlin data if JSON fails
6. Clear SharedPreferences on logout if needed
7. Don't store sensitive data unencrypted (use EncryptedSharedPreferences for medical info if required)

---

## 14. Related Documentation Files

- `TTS_ANDROID_WORKFLOW.md` — Text-to-Speech API workflow
- `VOICE_AI_ARCHITECTURE_GUIDE.md` — Voice assistant architecture
- `VOICE_AI_COMPLETE_WORKFLOW.md` — Voice AI workflow details
- `VOICE_AI_UI_MAPPING.md` — UI state mapping
- `DATABASE_FILES.md` — List of data files (if exists)

---

**End of Database Architecture Documentation**

For questions or updates, check the Manager classes in `app/src/main/java/com/example/firstaidapp/managers/` and data models in `app/src/main/java/com/example/firstaidapp/data/models/`.

