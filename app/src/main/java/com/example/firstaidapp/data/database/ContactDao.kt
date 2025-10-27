package com.example.firstaidapp.data.database

import androidx.room.*
import com.example.firstaidapp.data.models.ContactType
import com.example.firstaidapp.data.models.EmergencyContact
import kotlinx.coroutines.flow.Flow

// DAO for emergency contacts: queries and CRUD operations
@Dao
interface ContactDao {

    // Get all active contacts (defaults first)
    @Query("SELECT * FROM emergency_contacts WHERE isActive = 1 ORDER BY isDefault DESC, type ASC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    // Get active contacts for a specific state
    @Query("SELECT * FROM emergency_contacts WHERE state = :state AND isActive = 1 ORDER BY isDefault DESC, type ASC, name ASC")
    fun getContactsByState(state: String): Flow<List<EmergencyContact>>

    // Get contacts for state plus national services
    @Query("SELECT * FROM emergency_contacts WHERE (state = :state OR state = 'National') AND isActive = 1 ORDER BY isDefault DESC, type ASC, name ASC")
    fun getContactsByStateWithNational(state: String): Flow<List<EmergencyContact>>

    // Get contacts filtered by ContactType
    @Query("SELECT * FROM emergency_contacts WHERE type = :type AND isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun getContactsByType(type: ContactType): Flow<List<EmergencyContact>>

    // Search contacts by name (partial match)
    @Query("SELECT * FROM emergency_contacts WHERE name LIKE '%' || :searchQuery || '%' AND isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun searchContacts(searchQuery: String): Flow<List<EmergencyContact>>

    // Total count of contacts
    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactsCount(): Int

    // List of unique states with active contacts
    @Query("SELECT DISTINCT state FROM emergency_contacts WHERE isActive = 1 ORDER BY state ASC")
    suspend fun getAvailableStates(): List<String>

    // Insert or replace a contact
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    // Bulk insert/replace contacts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContact>)

    // Update an existing contact
    @Update
    suspend fun updateContact(contact: EmergencyContact)

    // Hard delete a contact
    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    // Soft delete (mark inactive)
    @Query("UPDATE emergency_contacts SET isActive = 0 WHERE id = :contactId")
    suspend fun softDeleteContact(contactId: Long)

    // Delete all contacts (dangerous)
    @Query("DELETE FROM emergency_contacts")
    suspend fun deleteAllContacts()

    // Delete only user-added contacts (keep defaults)
    @Query("DELETE FROM emergency_contacts WHERE isDefault = 0")
    suspend fun deleteUserContacts()

    // Get contact by ID
    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): EmergencyContact?
}
