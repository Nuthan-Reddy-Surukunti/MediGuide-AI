package com.example.firstaidapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Simple migration 1 -> 2: add relationship and notes to emergency_contacts
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    // Use parameter name 'db' to match supertype and avoid warnings
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE emergency_contacts ADD COLUMN relationship TEXT")
        db.execSQL("ALTER TABLE emergency_contacts ADD COLUMN notes TEXT")
    }
}
