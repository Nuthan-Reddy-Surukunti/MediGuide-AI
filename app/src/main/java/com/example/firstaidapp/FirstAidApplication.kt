package com.example.firstaidapp

// Application class: performs app-wide initialization

import android.app.Application

class FirstAidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Managers initialize on-demand when first ViewModels are created
        // No need for background worker - data loads from Kotlin sources quickly
    }
}