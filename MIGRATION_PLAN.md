# Migration Plan: Database → JSON Approach

## 📋 Implementation Plan

### Phase 1: Analysis & Preparation (5 min)
- [x] Analyze current database structure
- [x] Identify all data types and relationships
- [x] Map features to new approach
- [ ] Read current data initialization code
- [ ] Create backup plan

### Phase 2: Create New Data Structure (10 min)
- [ ] Create `assets` folder structure
- [ ] Generate `guides.json` from current data
- [ ] Generate `emergency_contacts.json` from current data
- [ ] Validate JSON files

### Phase 3: Create Manager Classes (15 min)
- [ ] Create `JsonGuideManager.kt` - handles guide data
- [ ] Create `ContactManager.kt` - handles contact data  
- [ ] Create `PreferencesManager.kt` - handles user data (favorites, view counts, search history)
- [ ] Add Gson dependency if needed

### Phase 4: Update ViewModels (10 min)
- [ ] Update `HomeViewModel.kt`
- [ ] Update `GuideDetailViewModel.kt`
- [ ] Update `ContactsViewModel.kt`
- [ ] Remove database references

### Phase 5: Update Fragments/UI (5 min)
- [ ] Update `HomeFragment.kt`
- [ ] Update any other fragments that reference database

### Phase 6: Cleanup (5 min)
- [ ] Remove database folder
- [ ] Remove Room dependencies from `build.gradle.kts`
- [ ] Remove DataInitializer
- [ ] Clean up unused imports

### Phase 7: Testing & Validation (5 min)
- [ ] Build project
- [ ] Fix any errors
- [ ] Verify all features work

---

## 🗂️ File Changes Overview

### Files to CREATE ✨
```
app/src/main/assets/
  ├── guides.json                          (NEW - all guide data)
  └── emergency_contacts.json              (NEW - default contacts)

app/src/main/java/com/example/firstaidapp/managers/
  ├── JsonGuideManager.kt                  (NEW - replaces GuideDao + GuideRepository)
  ├── ContactManager.kt                    (NEW - replaces ContactDao)
  └── PreferencesManager.kt                (NEW - user data storage)
```

### Files to UPDATE 🔄
```
app/build.gradle.kts                       (Remove Room, add Gson)
ui/home/HomeViewModel.kt                   (Use managers instead of database)
ui/home/HomeFragment.kt                    (Simplify initialization)
ui/guide/GuideDetailViewModel.kt           (Use managers)
ui/contacts/ContactsViewModel.kt           (Use managers)
```

### Files to DELETE ❌
```
data/database/AppDatabase.kt
data/database/GuideDao.kt
data/database/ContactDao.kt
data/database/SearchDao.kt
data/database/Converters.kt
data/database/Migrations.kt
data/repository/GuideRepository.kt
utils/DataInitializer.kt
utils/DataInitializationWorker.kt
```

---

## 🔄 Data Migration Map

### Current: Room Database Tables
```
first_aid_guides table
  → Becomes: guides.json (static data)
  → User data: SharedPreferences (favorites, viewCount, lastAccessed)

emergency_contacts table
  → Becomes: emergency_contacts.json (default contacts)
  → User contacts: SharedPreferences (user-added contacts)

search_history table
  → Becomes: SharedPreferences (search history list)

guide_steps table
  → Becomes: Part of guides.json (nested in each guide)
```

---

## 🎯 Feature Preservation Checklist

### Home Screen Features
- [ ] Display all guides
- [ ] Filter by category
- [ ] Search guides
- [ ] Show favorites
- [ ] Sort guides
- [ ] Navigate to guide details

### Guide Detail Features
- [ ] Show guide steps
- [ ] Toggle favorite
- [ ] Track view count
- [ ] Update last accessed
- [ ] Show warnings
- [ ] Display YouTube link
- [ ] Show emergency call button

### Contacts Features
- [ ] Display emergency contacts
- [ ] Filter by state
- [ ] Filter by type
- [ ] Search contacts
- [ ] Add custom contact
- [ ] Edit contact
- [ ] Delete contact
- [ ] Call contact

### Search Features
- [ ] Search guides
- [ ] Search contacts
- [ ] Search history
- [ ] Recent searches suggestions

---

## 📦 Dependencies Changes

### REMOVE from build.gradle.kts:
```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

### ADD to build.gradle.kts:
```kotlin
// Gson for JSON parsing
implementation("com.google.code.gson:gson:2.10.1")
```

---

## 🚀 Implementation Strategy

### Step-by-Step Approach:

1. **Start with data extraction**
   - Read DataInitializer to see current guide/contact data
   - Export to JSON format

2. **Build managers bottom-up**
   - PreferencesManager first (simplest)
   - JsonGuideManager second
   - ContactManager last

3. **Update ViewModels one at a time**
   - Test each ViewModel after update
   - Ensure no compile errors

4. **Remove old code carefully**
   - Keep backups
   - Remove one file at a time
   - Verify build after each removal

5. **Final validation**
   - Full build
   - Test all features
   - Document changes

---

## ⚠️ Risk Mitigation

### Potential Issues & Solutions:

**Issue 1**: Data loss during migration
- **Solution**: Keep database code until JSON is proven working

**Issue 2**: Missing features
- **Solution**: Feature checklist validation before removing database

**Issue 3**: Build errors
- **Solution**: Fix errors incrementally, one file at a time

**Issue 4**: Performance concerns
- **Solution**: Load JSON once, keep in memory (fast enough for this app size)

---

## 📝 Code Structure Preview

### New Architecture:
```
MainActivity
    ↓
HomeFragment
    ↓
HomeViewModel
    ↓
JsonGuideManager + PreferencesManager
    ↓
guides.json (read once) + SharedPreferences (read/write)
```

**vs Old Architecture:**
```
MainActivity
    ↓
HomeFragment
    ↓
HomeViewModel
    ↓
GuideRepository
    ↓
GuideDao + ContactDao + SearchDao
    ↓
AppDatabase (Room)
    ↓
SQLite Database + Migrations + TypeConverters
```

**Much simpler! 🎉**

---

## ✅ Success Criteria

Migration is successful when:
1. ✅ App builds without errors
2. ✅ All features work as before
3. ✅ No database-related code remains
4. ✅ Code is simpler and easier to understand
5. ✅ App runs smoothly without crashes
6. ✅ Data persists across app restarts (favorites, user contacts, etc.)

---

## 🎬 Ready to Start!

**Estimated total time**: 45-60 minutes

**Current status**: Plan complete ✅

**Next step**: Begin Phase 1 - Read current data initialization

---

Let's do this! 🚀

