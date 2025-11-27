package com.mediguide.firstaid.utils

import android.app.AlertDialog
import android.content.Context

// Simple dialog utilities used across the app (welcome, reminders, permissions)

/**
 * Simple dialog helper that combines all dialog functionality
 * Replaces LearningDialogManager and permission dialogs from MainActivity
 */
object DialogHelper {

    /**
     * Show welcome dialog for first-time users
     */
    fun showWelcomeDialog(context: Context, onComplete: () -> Unit) {
        val prefsManager = UserPreferencesManager(context)
        if (prefsManager.onboardingCompleted) {
            onComplete()
            return
        }

        AlertDialog.Builder(context)
            .setTitle("Welcome to MediGuide AI! 📚")
            .setMessage(
                "⚠️ EDUCATIONAL USE ONLY\n" +
                "This app is for learning first aid techniques. Not a substitute for professional medical advice. Always call emergency services (112) in real emergencies.\n\n" +
                "📚 This app contains 10 educational first aid guides\n\n" +
                "💡 We recommend:\n" +
                "• Reading through all training guides\n" +
                "• Reviewing educational content\n" +
                "• Learning reference techniques\n\n" +
                "🔔 Would you like daily reminders to continue your training?"
            )
            .setPositiveButton("Enable Reminders") { dialog, _ ->
                prefsManager.learningRemindersEnabled = true
                prefsManager.onboardingCompleted = true

                // Schedule notifications via unified manager
                LearningNotificationManager(context).scheduleDailyNotifications()
                dialog.dismiss()
                showNotificationFrequencyDialog(context, onComplete)
            }
            .setNegativeButton("Maybe Later") { dialog, _ ->
                prefsManager.learningRemindersEnabled = false
                prefsManager.onboardingCompleted = true
                dialog.dismiss()
                onComplete()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show notification frequency selection
     */
    private fun showNotificationFrequencyDialog(context: Context, onComplete: () -> Unit) {
        val prefsManager = UserPreferencesManager(context)
        val frequencies = arrayOf("Daily", "Every 2 Days", "Weekly")
        val frequencyValues = arrayOf("daily", "every_2_days", "weekly")
        var selectedIndex = frequencyValues.indexOf(prefsManager.notificationFrequency).coerceAtLeast(0)

        AlertDialog.Builder(context)
            .setTitle("Notification Frequency")
            .setSingleChoiceItems(frequencies, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Set") { dialog, _ ->
                prefsManager.notificationFrequency = frequencyValues[selectedIndex]

                // Use unified scheduler
                LearningNotificationManager(context).scheduleDailyNotifications()
                dialog.dismiss()
                onComplete()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show daily learning reminder
     */
    fun showDailyReminderDialog(context: Context, onStartLearning: () -> Unit = {}) {
        val notificationManager = LearningNotificationManager(context)

        if (!notificationManager.shouldShowDailyReminder()) {
            return
        }

        val prefsManager = UserPreferencesManager(context)
        val openedCount = prefsManager.getOpenedGuidesCount()
        val totalCount = prefsManager.totalGuidesCount

        val message = if (openedCount == 0) {
            "You haven't explored any training guides yet!\n\n" +
            "📖 Start your educational journey today.\n\n" +
            "Review a guide on the home screen to begin learning."
        } else if (openedCount < totalCount) {
            "Great progress! You've studied $openedCount out of $totalCount guides.\n\n" +
            "🎯 Continue your first aid education today!\n\n" +
            "Learn more reference techniques."
        } else {
            "Amazing! You've reviewed all $totalCount guides! 🎉\n\n" +
            "📚 Consider re-reading guides for better retention.\n\n" +
            "Regular review helps reinforce your training!"
        }

        AlertDialog.Builder(context)
            .setTitle("Daily Learning Reminder 📚")
            .setMessage(message)
            .setPositiveButton("Start Learning") { dialog, _ ->
                // mark shown via unified manager
                notificationManager.markReminderShownToday()
                dialog.dismiss()
                onStartLearning()
            }
            .setNegativeButton("Later") { dialog, _ ->
                notificationManager.markReminderShownToday()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Show permission rationale dialog
     */
    fun showPermissionRationaleDialog(context: Context, onAllow: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Permissions Required")
            .setMessage(
                "🎙️ Microphone: For AI voice commands\n\n" +
                "📍 Location: To find nearby hospitals\n\n" +
                "📞 Phone: To make quick calls"
            )
            .setPositiveButton("Allow") { _, _ -> onAllow() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Show permission denied dialog
     */
    fun showPermissionDeniedDialog(context: Context, deniedPermissions: List<String>) {
        val permissionNames = deniedPermissions.map { permission ->
            when (permission) {
                android.Manifest.permission.RECORD_AUDIO -> "Microphone"
                android.Manifest.permission.ACCESS_FINE_LOCATION -> "Location"
                android.Manifest.permission.CALL_PHONE -> "Phone"
                else -> "Unknown"
            }
        }.joinToString(", ")

        AlertDialog.Builder(context)
            .setTitle("Permissions Denied")
            .setMessage(
                "The following permissions were denied: $permissionNames\n\n" +
                "Some features may not work properly. You can enable them later in Settings."
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Show simple alert dialog
     */
    fun showSimpleAlert(context: Context, title: String, message: String, onOk: (() -> Unit)? = null) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onOk?.invoke()
            }
            .show()
    }
}
