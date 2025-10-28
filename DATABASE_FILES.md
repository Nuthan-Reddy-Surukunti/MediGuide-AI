# Database folder — files, explanations, and where they're used

This document explains the files under `app/src/main/java/com/example/firstaidapp/data/database/`.
For each file you will find:
- What it contains / purpose
- Key classes, methods, and behaviors
- How Room is configured (if applicable)
- Where in the app the code is used (call sites / consumers)

Files covered:
- `AppDatabase.kt`
- `Converters.kt`
- `ContactDao.kt`
- `GuideDao.kt`
- `SearchDao.kt`
- `Migrations.kt`

---

## 1) AppDatabase.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/AppDatabase.kt`

Purpose
- The central Room database definition. Declares entities, version, type converters and exposes DAOs.
- Also contains migration objects and a thread-safe singleton `getDatabase()` factory.

Key points / code explanation
- Annotated with `@Database(entities = [...], version = 10, exportSchema = false)` listing the entities: `FirstAidGuide`, `GuideStep`, `EmergencyContact`, `SearchHistory`.
- Uses `@TypeConverters(Converters::class)` so Room can store complex types (enums, lists, etc.).
- Declares abstract DAO accessors: `guideDao()`, `contactDao()`, `searchDao()`.
- Companion object provides a `@Volatile private var INSTANCE` and `getDatabase(context)` which builds the Room database singleton.
- Migrations: several `Migration(x, y)` objects are defined inline for versions 3->4, 4->5, 5->6, 6->7, 7->8, 8->9 and `addMigrations(...)` is called in the builder. `fallbackToDestructiveMigration()` is also included as a final fallback.
  - Examples of migration actions: adding columns to tables, creating new tables (guide_steps), removing duplicates, creating unique indexes, and recreating corrected tables.

Where it's used (call sites)
- DataInitializer.kt — calls `AppDatabase.getDatabase(context)` to populate guides and contacts on first run.
- `HomeFragment.kt` — `AppDatabase.getDatabase(requireContext())` to create a `GuideRepository` backed by the DAOs.
- `HomeViewModel.kt` — obtains database in `init` and builds `GuideRepository`.
- `ContactsViewModel.kt` — gets `AppDatabase.getDatabase(application)` and uses `contactDao()`.
- `GuideDetailViewModel.kt` — gets database and builds `GuideRepository`.

Why it matters
- Central point for database lifecycle, migrations and DAO availability. Changing this file affects schema/versioning and all database consumers.

---

## 2) Converters.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/Converters.kt`

Purpose
- Contains Room `@TypeConverter` methods to convert non-primitive types to and from a type Room can persist (usually `String`/JSON).

Key points / code explanation
- Uses Gson to serialize/deserialize lists to JSON strings.
- Converters included:
  - `ContactType <-> String` (enum to its name and back)
  - `List<GuideStep> <-> String` (serialize list of GuideStep to JSON and back)
  - `List<String> <-> String` (generic string lists)
  - `StepType <-> String` (enum conversion with a fallback for unknown values)
- Methods are annotated with `@TypeConverter` and are used automatically by Room when `@TypeConverters(Converters::class)` is registered on the database.

Where it's used
- Registered in `AppDatabase.kt` via `@TypeConverters(Converters::class)` and therefore used by all entities that have fields needing conversion, e.g. `FirstAidGuide.steps: List<GuideStep>`, fields using `ContactType` and `StepType`.

Important considerations
- Gson-based serialization means the persisted JSON shape must match `GuideStep` model structure. Any breaking changes in the model may require migration or clearing stored JSON.

---

## 3) ContactDao.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/ContactDao.kt`

Purpose
- DAO (Data Access Object) that defines queries and persistence operations for the `emergency_contacts` table (mapped to `EmergencyContact` model).

Key points / code explanation
- Returns Kotlin Flow for most read queries to support reactive observation (`Flow<List<EmergencyContact>>`).
- Common queries provided:
  - `getAllContacts()` — active contacts ordered (defaults first, then by type and name)
  - `getContactsByState(state)` — filter by state
  - `getContactsByStateWithNational(state)` — returns contacts for a state plus national ones
  - `getContactsByType(type: ContactType)` — filter by enum type
  - `searchContacts(searchQuery)` — name partial-match
  - `getContactsCount()` — total rows count (suspend)
  - `getAvailableStates()` — distinct active states (suspend)
- Insert/update/delete operations:
  - `insertContact(contact: EmergencyContact): Long` with `OnConflictStrategy.REPLACE`
  - `insertContacts(contacts: List<EmergencyContact>)`
  - `updateContact(contact: EmergencyContact)`
  - `deleteContact(contact: EmergencyContact)` (hard delete)
  - `softDeleteContact(contactId: Long)` — sets `isActive = 0` for defaults preservation
  - `deleteAllContacts()` and `deleteUserContacts()` (delete only user-added ones)
  - `getContactById(id)` returns a nullable contact (suspend)

Where it's used (call sites in app)
- `DataInitializer.initializeContacts(database)` — inserts initial contact list using `contactDao().insertContact(...)`.
- `ContactsViewModel.kt` — obtains `contactDao()` and calls many methods:
  - `getAllContacts()`, `getContactsByStateWithNational(state)`, `searchContacts(query)`, `getAvailableStates()`, `insertContact`, `updateContact`, `deleteContact`, `softDeleteContact` etc.
- `HomeFragment.kt` and `HomeViewModel.kt` when constructing repositories: the `GuideRepository` receives `contactDao()` and may call contact-related queries where needed (e.g., emergency contact lookups from home UI).
- `GuideRepository` (used by multiple ViewModels) — often receives `contactDao()` in its constructor (used indirectly across the UI layers).

Why it matters
- Provides the contact data used throughout the app for emergency dialing, contact lists and filtering by state/type. Soft-delete and defaults handling enables preserving built-in contacts while allowing user additions.

---

## 4) GuideDao.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/GuideDao.kt`

Purpose
- DAO for `first_aid_guides` table (maps to `FirstAidGuide` entity). Handles guide queries, searches, favorites and simple updates.

Key points / code explanation
- Exposure via LiveData for many reads to easily observe changes in the UI (`LiveData<List<FirstAidGuide>>`).
- Common queries provided:
  - `getAllGuides()` — returns all guides ordered by category then title
  - `getGuideById(guideId: String)` — returns single guide as LiveData
  - `getGuidesByCategory(category: String)`
  - `searchGuides(query: String)` (LiveData) and `searchGuidesList(query: String)` (suspend returning List)
  - `getFavoriteGuides()` — favorites ordered by lastAccessedTimestamp
  - `getAllCategories()` — distinct categories
  - `getGuidesCount()` — suspend count
- Mutation operations:
  - `insertGuide(guide)` and `insertAll(guides)` with `OnConflictStrategy.REPLACE`
  - `updateGuide(guide)` (annotated `@Update`)
  - `updateFavoriteStatus(guideId, isFavorite)` — updates favorite flag
  - `updateLastAccessed(guideId, timestamp)` — used when a guide is viewed
  - `deleteGuide(guide)` and `deleteAllGuides()`

Where it's used (call sites in app)
- `DataInitializer.initializeGuides(database)` — inserts initial guide data using `guideDao().insertGuide(...)`.
- `HomeFragment.kt` / `HomeViewModel.kt` — `GuideRepository` is created with `guideDao()` and the UI observes `allGuides` (coming from repository, which queries `getAllGuides()`), performs searches and navigation to details.
- `GuideDetailViewModel.kt` — repository obtains guide by id (via DAO) and updates last accessed & favorite status using DAO-backed functions.
- `GuideRepository` — the repository wraps `GuideDao` calls; ViewModels call repository methods such as `searchGuidesList`, `toggleFavorite`, `updateLastAccessed` which under the hood call these DAO methods.

Why it matters
- Core source for all guide content displayed in the app. Efficient queries and LiveData make it simple for the UI to observe updates.

---

## 5) SearchDao.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/SearchDao.kt`

Purpose
- DAO for `search_history` table (maps to `SearchHistory` entity). Stores recent search records and supports returning recent queries for quick suggestions.

Key points / code explanation
- `getRecentSearches()` — returns last 10 rows ordered by timestamp as `LiveData<List<SearchHistory>>`.
- `getRecentSearchQueries()` — returns last 5 distinct query strings (`LiveData<List<String>>`). Note: query uses DISTINCT and aliasing.
- `insertSearch(search: SearchHistory)` — insert with `OnConflictStrategy.REPLACE`.
- `deleteOldSearches(cutoffTime: Long)` — deletes older records (maintenance)
- `clearSearchHistory()` — clears table

Where it's used (call sites in app)
- `HomeFragment.kt` and `HomeViewModel.kt` create repository objects that receive `searchDao()`; repository methods use `searchDao()` to store user searches and to provide recent suggestions when the user types a query.
- Search UI (search-related fragments / viewmodels) are expected to call `insertSearch` when a user executes a search and observe `getRecentSearchQueries()` to show suggestions — specific search UI files will interact via the repository layer.

Why it matters
- Improves user experience by showing recent searches and suggestions; also lets the app maintain a small history for quick access.

---

## 6) Migrations.kt
Path: `app/src/main/java/com/example/firstaidapp/data/database/Migrations.kt`

Purpose
- Contains additional Migration objects (example: `MIGRATION_1_2`) used to evolve older schema versions. This file complements inline migrations in `AppDatabase.kt`.

Key points / code explanation
- Example included: `MIGRATION_1_2` which adds `relationship` and `notes` columns to `emergency_contacts` via `ALTER TABLE` statements.
- The project includes several migration definitions across files; `AppDatabase` also defines migrations for higher versions (3->4 ... 8->9). Taken together they attempt to migrate older databases safely.

Where it's used
- Migration objects from this file can be added to the Room builder in `AppDatabase.getDatabase()`. (Note: the current `AppDatabase.kt` adds migrations defined in its companion; check code before adding additional migrations.)

Why it matters
- Proper migrations prevent destructive schema updates and preserve user data across app updates. If migrations are incomplete or missing, `fallbackToDestructiveMigration()` may wipe the DB.

---

Appendix: Quick map of callers / files that use database components
- `app/src/main/java/com/example/firstaidapp/utils/DataInitializer.kt` — bootstraps initial guides and contacts using `AppDatabase.getDatabase()` and DAO insert methods.
- `app/src/main/java/com/example/firstaidapp/ui/home/HomeFragment.kt` — creates `GuideRepository` using DAOs from `AppDatabase` and calls repository search methods to navigate to guides.
- `app/src/main/java/com/example/firstaidapp/ui/home/HomeViewModel.kt` — obtains `AppDatabase`/DAOs to create `GuideRepository` and observes `allGuides`.
- `app/src/main/java/com/example/firstaidapp/ui/contacts/ContactsViewModel.kt` — uses `contactDao()` extensively to provide contact lists, search, add/update/delete and available states.
- `app/src/main/java/com/example/firstaidapp/ui/guide/GuideDetailViewModel.kt` — uses repository (backed by `guideDao`) to load a specific guide, update last accessed timestamp and toggle favorite.
- `app/src/main/java/com/example/firstaidapp/data/repository/GuideRepository.kt` (and other repository classes) — wrap DAO operations and are the main API the UI/VMs call. (Repository files receive DAOs created from `AppDatabase.getDatabase()`.)

Notes & suggestions
- If you change entity fields (or the `GuideStep`/`FirstAidGuide` data model JSON structure) ensure the converters and migrations are updated. Gson serialization of `GuideStep` depends on the model's shape.
- Migrations are partially defined across `AppDatabase.kt` and `Migrations.kt`. Review that all expected migrations are registered in `getDatabase()` to avoid unexpected destructive migrations in the field.
- The DB builder includes `.fallbackToDestructiveMigration()` — acceptable during development, but for production you should provide complete migrations to avoid data loss.

If you'd like, I can:
- Generate a cross-reference table listing the exact method calls from each ViewModel/repository into each DAO.
- Create a small UML-like diagram (text) showing relations between entities, DAOs, repositories and ViewModels.

---

Generated on: October 27, 2025

