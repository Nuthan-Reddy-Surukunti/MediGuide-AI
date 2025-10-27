package com.example.firstaidapp.utils

// (Legacy) Manager that orchestrated learning dialogs (kept for compatibility)

import android.content.Context

/**
 * Thin compatibility wrapper that delegates to the unified DialogHelper and LearningNotificationManager.
 * This preserves old call sites while avoiding duplicate implementation.
 */
class LearningDialogManager(private val context: Context) {

    /**
     * Show welcome dialog for first-time users
     */
    fun showWelcomeDialog(onComplete: () -> Unit) {
        DialogHelper.showWelcomeDialog(context, onComplete)
    }

    /**
     * Show daily learning reminder dialog
     */
    fun showDailyReminderDialog(onStartLearning: () -> Unit = {}) {
        DialogHelper.showDailyReminderDialog(context, onStartLearning)
    }

    /**
     * Show progress dialog when user completes reading all guides
     */
    fun showCompletionDialog() {
        DialogHelper.showSimpleAlert(
            context,
            "🎉 Congratulations!",
            "You've explored all first aid guides!\n\nYou're now better prepared to handle emergencies.")
    }

    /**
     * Show settings dialog for learning reminders
     */
    fun showReminderSettingsDialog() {
        val prefsManager = UserPreferencesManager(context)
        val currentlyEnabled = prefsManager.learningRemindersEnabled
        val currentFrequency = prefsManager.notificationFrequency
        val frequencyText = when (currentFrequency) {
            "daily" -> "Daily"
            "every_2_days" -> "Every 2 Days"
            "weekly" -> "Weekly"
            else -> "Daily"
        }

        val message = "Current Status: ${if (currentlyEnabled) "Enabled" else "Disabled"}\n" +
                "Frequency: $frequencyText\n" +
                "Time: ${prefsManager.notificationTimeHour}:00\n\n" +
                "What would you like to do?"

        DialogHelper.showSimpleAlert(context, "Learning Reminder Settings", message)
    }
}
