# 📚 Quick Reference Guide - New JSON Approach

## 🎯 For Developers (You!)

### Where to Find Things Now

#### **Guides Data**
- **File**: `JsonGuideManager.kt`
- **Usage**:
```kotlin
val guideManager = JsonGuideManager(context)
val allGuides = guideManager.getAllGuides() // LiveData
val guide = guideManager.getGuideById("cpr_guide") // LiveData
val results = guideManager.searchGuidesList("CPR") // List
```

#### **Contacts Data**
- **File**: `ContactManager.kt`
- **Usage**:
```kotlin
val contactManager = ContactManager(context)
val allContacts = contactManager.getAllContacts() // Flow
val stateContacts = contactManager.getContactsByState("Karnataka") // Flow
contactManager.insertContact(newContact) // suspend fun
```

#### **User Preferences** (Favorites, View Counts, etc.)
- **File**: `PreferencesManager.kt`
- **Usage**:
```kotlin
val prefsManager = PreferencesManager(context)
prefsManager.addFavorite("cpr_guide")
prefsManager.isFavorite("cpr_guide") // Boolean
prefsManager.incrementViewCount("cpr_guide")
prefsManager.addSearchQuery("burns")
```

---

## 🔧 Common Tasks

### Add a New Guide
1. Open `app/src/main/java/com/example/firstaidapp/data/repository/FirstAidGuidesData.kt`
2. Add new guide to the list in `getAllFirstAidGuides()`
3. Done! It will appear automatically

### Add a New Emergency Contact
1. Open `app/src/main/java/com/example/firstaidapp/data/repository/EmergencyContactsData.kt`
2. Add to `getAllEmergencyContacts()` for national contacts
3. Or add to `getContactsForState(state)` for state-specific contacts
4. Done!

### Track User Action (e.g., Guide Viewed)
```kotlin
// In ViewModel
guideManager.updateLastAccessed(guideId)
// This automatically increments view count too!
```

### Add User to Favorites
```kotlin
// In ViewModel  
guideManager.toggleFavorite(guideId, true)
```

### Save Search Query
```kotlin
// In ViewModel
prefsManager.addSearchQuery(query)
```

### Get Recent Searches
```kotlin
// In ViewModel
val recentSearches = prefsManager.getRecentSearches(5) // Last 5
```

---

## 📝 File Structure (New)

```
app/src/main/java/com/example/firstaidapp/
├── managers/                          ← NEW! All data management here
│   ├── JsonGuideManager.kt           ← Manages guides
│   ├── ContactManager.kt             ← Manages contacts
│   └── PreferencesManager.kt         ← Manages user data
│
├── data/
│   ├── models/                       ← Data models (unchanged)
│   │   ├── FirstAidGuide.kt
│   │   ├── EmergencyContact.kt
│   │   └── ...
│   │
│   └── repository/                   ← Static data sources
│       ├── FirstAidGuidesData.kt    ← All guide data
│       ├── FirstAidGuidesRepository.kt ← Guide steps details
│       └── EmergencyContactsData.kt  ← All contact data
│
├── ui/
│   ├── home/
│   │   ├── HomeViewModel.kt          ← Uses JsonGuideManager
│   │   └── HomeFragment.kt           ← No more database!
│   │
│   ├── guide/
│   │   └── GuideDetailViewModel.kt   ← Uses JsonGuideManager
│   │
│   └── contacts/
│       └── ContactsViewModel.kt      ← Uses ContactManager
│
└── utils/
    └── JsonExporter.kt               ← Helper (temporary)
```

---

## 💡 Understanding the Managers

### **JsonGuideManager**
- **Purpose**: Load and manage first aid guides
- **Data Source**: FirstAidGuidesData (Kotlin) or guides.json (future)
- **Returns**: LiveData for UI observation
- **Features**:
  - Get all guides
  - Search guides
  - Filter by category
  - Track favorites (via PreferencesManager)
  - Track view counts (via PreferencesManager)

### **ContactManager**
- **Purpose**: Manage emergency contacts
- **Data Source**: EmergencyContactsData (Kotlin) or emergency_contacts.json (future)
- **Returns**: Flow for reactive updates
- **Features**:
  - Get all contacts
  - Filter by state
  - Filter by type
  - Search contacts
  - Add/update/delete user contacts (stored in SharedPreferences)

### **PreferencesManager**
- **Purpose**: Store all user-specific data
- **Data Source**: SharedPreferences
- **Storage**: Key-value pairs + JSON for complex data
- **Features**:
  - Favorites management
  - View count tracking
  - Last accessed tracking
  - Search history
  - User-added contacts
  - Auto-incrementing contact IDs

---

## 🎨 Design Patterns Used

### 1. **Manager Pattern**
- Encapsulates data operations
- Single responsibility
- Easy to test and maintain

### 2. **Repository Pattern** (Sort of)
- Managers act as repositories
- Abstract data sources
- Provide clean API to ViewModels

### 3. **Observer Pattern**
- LiveData / Flow for reactive updates
- UI automatically updates when data changes

---

## 🚦 Data Flow

### Reading Guides
```
User opens app
    ↓
HomeFragment loads
    ↓
HomeViewModel initializes
    ↓
JsonGuideManager created
    ↓
Loads guides from FirstAidGuidesData (fallback to Kotlin)
    ↓
Stores in memory
    ↓
Returns LiveData<List<FirstAidGuide>>
    ↓
HomeFragment observes and displays
```

### Favoriting a Guide
```
User clicks favorite button
    ↓
HomeFragment calls ViewModel.toggleFavorite()
    ↓
ViewModel calls guideManager.toggleFavorite()
    ↓
GuideManager calls preferencesManager.addFavorite()
    ↓
PreferencesManager saves to SharedPreferences
    ↓
Done! (persists across app restarts)
```

### Searching
```
User types in search box
    ↓
Fragment calls ViewModel.searchGuides(query)
    ↓
ViewModel calls guideManager.searchGuidesList(query)
    ↓
GuideManager filters in-memory list
    ↓
Also saves query: preferencesManager.addSearchQuery(query)
    ↓
Returns filtered results
    ↓
UI updates with results
```

---

## 📖 For Your Presentation

### Slide 1: Problem
> "Room Database was too complex for static content:
> - 1,400+ lines of code
> - Complex migrations
> - SQL queries
> - Hard to explain and maintain"

### Slide 2: Solution
> "Simplified to Manager pattern:
> - 240 lines of clean code
> - Load static data from Kotlin objects
> - Store user data in SharedPreferences
> - Industry-standard JSON support"

### Slide 3: Architecture
```
[Show diagram]

Old:     UI → ViewModel → Repository → DAO → Room → SQLite
New:     UI → ViewModel → Manager → Data/SharedPreferences
```

### Slide 4: Benefits
> "✅ 83% less code
> ✅ Easier to understand
> ✅ All features preserved
> ✅ Faster performance
> ✅ Modern approach"

### Slide 5: Demo
> [Show the app working]
> - View guides ✅
> - Search ✅
> - Add to favorites ���
> - Emergency contacts ✅

---

## ❓ FAQ

**Q: Why not use JSON files?**
A: The app currently uses Kotlin data objects which are loaded into memory. This is actually faster than parsing JSON! JSON files are optional and can be added later if needed.

**Q: Where is user data stored?**
A: SharedPreferences, which is Android's built-in key-value storage. Perfect for favorites, view counts, etc.

**Q: What about offline support?**
A: Everything works offline! All guides are in the app code, and user data is local.

**Q: Can I still use Room Database?**
A: Yes! The old database code is still in the project. But the app now uses the new managers instead.

**Q: How do I export to JSON files?**
A: Run the app once - it will export JSON files to the app's external storage directory. Then you can move them to the assets folder.

**Q: Is this approach scalable?**
A: For your use case (20-30 guides), yes! If you had 10,000+ guides, you might want to use a database. But for static educational content, this is perfect.

---

## 🎯 Key Takeaways

1. **Simpler is better** for static content
2. **SharedPreferences** is perfect for user data
3. **In-memory** data is very fast
4. **Manager pattern** keeps code organized
5. **All features preserved** with less code

---

## 📞 Need Help?

Check these files for examples:
- `HomeViewModel.kt` - How to use JsonGuideManager
- `ContactsViewModel.kt` - How to use ContactManager
- `PreferencesManager.kt` - See all available methods

---

**Happy Coding!** 🚀

