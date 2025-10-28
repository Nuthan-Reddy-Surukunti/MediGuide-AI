package com.example.firstaidapp.managers

import android.content.Context
import com.example.firstaidapp.data.models.ContactType
import com.example.firstaidapp.data.models.EmergencyContact
import com.example.firstaidapp.data.repository.EmergencyContactsData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Manages emergency contacts from JSON file and user-added contacts from preferences.
 * Combines default contacts from JSON with user contacts from SharedPreferences.
 * Falls back to Kotlin data source if JSON is not available.
 */
class ContactManager(private val context: Context) {

    private val gson = Gson()
    private val preferencesManager = PreferencesManager(context)

    private var defaultContacts: List<EmergencyContact> = emptyList()
    private val _contactsFlow = MutableStateFlow<List<EmergencyContact>>(emptyList())

    init {
        loadContacts()
        refreshContacts()
    }

    /**
     * Load default contacts from JSON file in assets folder, or fallback to Kotlin data
     */
    private fun loadContacts() {
        try {
            val jsonString = context.assets.open("emergency_contacts.json")
                .bufferedReader()
                .use { it.readText() }
                .trim()

            // Check if JSON is empty or just an empty array
            if (jsonString.isNotBlank() && jsonString != "[]" && jsonString.length > 10) {
                val type = object : TypeToken<List<EmergencyContact>>() {}.type
                val contactsFromJson: List<EmergencyContact>? = gson.fromJson(jsonString, type)

                if (contactsFromJson != null && contactsFromJson.isNotEmpty()) {
                    defaultContacts = contactsFromJson
                    android.util.Log.d("ContactManager", "Loaded ${defaultContacts.size} contacts from JSON")
                } else {
                    // Fallback to Kotlin data source
                    defaultContacts = EmergencyContactsData.getAllEmergencyContactsWithStates()
                    android.util.Log.d("ContactManager", "JSON empty, loaded ${defaultContacts.size} contacts from Kotlin data")
                }
            } else {
                // Fallback to Kotlin data source
                defaultContacts = EmergencyContactsData.getAllEmergencyContactsWithStates()
                android.util.Log.d("ContactManager", "JSON invalid/empty, loaded ${defaultContacts.size} contacts from Kotlin data")
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactManager", "Error loading contacts from JSON, using Kotlin fallback", e)
            // Fallback to Kotlin data source
            defaultContacts = EmergencyContactsData.getAllEmergencyContactsWithStates()
        }

        android.util.Log.d("ContactManager", "Final default contacts count: ${defaultContacts.size}")
    }

    /**
     * Refresh the combined list of default + user contacts
     */
    private fun refreshContacts() {
        val combined = defaultContacts + preferencesManager.getUserContacts()
        _contactsFlow.value = combined
    }

    // ==================== Get All Contacts ====================

    fun getAllContacts(): Flow<List<EmergencyContact>> {
        return _contactsFlow.map { contacts ->
            contacts
                .filter { it.isActive }
                .sortedWith(
                    compareByDescending<EmergencyContact> { it.isDefault }
                        .thenBy { it.type.ordinal }
                        .thenBy { it.name }
                )
        }
    }

    fun getAllContactsList(): List<EmergencyContact> {
        return _contactsFlow.value
            .filter { it.isActive }
            .sortedWith(
                compareByDescending<EmergencyContact> { it.isDefault }
                    .thenBy { it.type.ordinal }
                    .thenBy { it.name }
            )
    }

    // ==================== Get by State ====================

    fun getContactsByState(state: String): Flow<List<EmergencyContact>> {
        return _contactsFlow.map { contacts ->
            contacts
                .filter { it.isActive && it.state == state }
                .sortedWith(
                    compareByDescending<EmergencyContact> { it.isDefault }
                        .thenBy { it.type.ordinal }
                        .thenBy { it.name }
                )
        }
    }

    fun getContactsByStateWithNational(state: String): Flow<List<EmergencyContact>> {
        return _contactsFlow.map { contacts ->
            contacts
                .filter { it.isActive && (it.state == state || it.state == "National") }
                .sortedWith(
                    compareByDescending<EmergencyContact> { it.isDefault }
                        .thenBy { it.type.ordinal }
                        .thenBy { it.name }
                )
        }
    }

    fun getContactsByStateSync(state: String): List<EmergencyContact> {
        return _contactsFlow.value
            .filter { it.isActive && (it.state == state || it.state == "National") }
            .sortedWith(
                compareByDescending<EmergencyContact> { it.isDefault }
                    .thenBy { it.type.ordinal }
                    .thenBy { it.name }
            )
    }

    // ==================== Get by Type ====================

    fun getContactsByType(type: ContactType): Flow<List<EmergencyContact>> {
        return _contactsFlow.map { contacts ->
            contacts
                .filter { it.isActive && it.type == type }
                .sortedWith(
                    compareByDescending<EmergencyContact> { it.isDefault }
                        .thenBy { it.name }
                )
        }
    }

    // ==================== Search ====================

    fun searchContacts(query: String): Flow<List<EmergencyContact>> {
        return _contactsFlow.map { contacts ->
            if (query.isBlank()) {
                contacts.filter { it.isActive }
            } else {
                contacts.filter { contact ->
                    contact.isActive && (
                        contact.name.contains(query, ignoreCase = true) ||
                        contact.phoneNumber.contains(query) ||
                        contact.state.contains(query, ignoreCase = true) ||
                        contact.description?.contains(query, ignoreCase = true) == true
                    )
                }
            }.sortedWith(
                compareByDescending<EmergencyContact> { it.isDefault }
                    .thenBy { it.type.ordinal }
                    .thenBy { it.name }
            )
        }
    }

    // ==================== Get Contact by ID ====================

    fun getContactById(id: Long): EmergencyContact? {
        return _contactsFlow.value.find { it.id == id }
    }

    // ==================== Add/Update/Delete User Contacts ====================

    suspend fun insertContact(contact: EmergencyContact): Long {
        val newContact = if (contact.id == 0L) {
            contact.copy(
                id = preferencesManager.getNextContactId(),
                isDefault = false
            )
        } else {
            contact
        }

        preferencesManager.addUserContact(newContact)
        refreshContacts()
        return newContact.id
    }

    suspend fun updateContact(contact: EmergencyContact) {
        preferencesManager.updateUserContact(contact)
        refreshContacts()
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        if (contact.isDefault) {
            // For default contacts, do soft delete
            softDeleteContact(contact.id)
        } else {
            // For user contacts, hard delete
            preferencesManager.deleteUserContact(contact.id)
            refreshContacts()
        }
    }

    suspend fun softDeleteContact(contactId: Long) {
        val contact = getContactById(contactId) ?: return
        val updated = contact.copy(isActive = false)
        preferencesManager.updateUserContact(updated)
        refreshContacts()
    }

    suspend fun deleteAllContacts() {
        // Only delete user contacts, keep defaults
        val userContacts = preferencesManager.getUserContacts()
        userContacts.forEach { preferencesManager.deleteUserContact(it.id) }
        refreshContacts()
    }

    // ==================== Get Available States ====================

    suspend fun getAvailableStates(): List<String> {
        return _contactsFlow.value
            .filter { it.isActive && it.state != "National" }
            .map { it.state }
            .distinct()
            .sorted()
    }

    // ==================== Get Counts ====================

    suspend fun getContactsCount(): Int {
        return _contactsFlow.value.count { it.isActive }
    }

    fun getDefaultContactsCount(): Int {
        return defaultContacts.size
    }

    fun getUserContactsCount(): Int {
        return preferencesManager.getUserContacts().size
    }
}

