package com.example.firstaidapp.utils

import android.content.Context

/**
 * Lightweight facade for notification actions used by legacy callers.
 * Delegates to LearningNotificationManager to keep scheduling and prefs in one place.
 */
object NotificationHelper {

    private fun getManager(context: Context) = LearningNotificationManager(context)

    fun shouldShowDailyReminder(context: Context): Boolean {
        return getManager(context).shouldShowDailyReminder()
    }

    fun scheduleDailyNotifications(context: Context) {
        getManager(context).scheduleDailyNotifications()
    }
}
