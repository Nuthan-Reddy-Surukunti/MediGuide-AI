package com.mediguide.firstaid.managers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.mediguide.firstaid.utils.UserPreferencesManager

/**
 * Manages vibration feedback throughout the app
 * Respects user's vibration preference setting
 */
class VibrationManager(private val context: Context) {

    private val preferencesManager = UserPreferencesManager(context)
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Vibrate for a short duration (button click feedback)
     */
    fun vibrateClick() {
        vibrate(50)
    }

    /**
     * Vibrate for a medium duration (notification)
     */
    fun vibrateNotification() {
        vibrate(100)
    }

    /**
     * Vibrate for a long duration (alert/error)
     */
    fun vibrateAlert() {
        vibrate(200)
    }

    /**
     * Vibrate with a custom duration
     */
    fun vibrate(durationMillis: Long) {
        if (!preferencesManager.isVibrationEnabled) {
            return
        }

        if (!vibrator.hasVibrator()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    /**
     * Vibrate with a pattern
     * @param pattern Array of durations in milliseconds (off, on, off, on, ...)
     */
    fun vibratePattern(pattern: LongArray) {
        if (!preferencesManager.isVibrationEnabled) {
            return
        }

        if (!vibrator.hasVibrator()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Cancel any ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }

    companion object {
        @Volatile
        private var instance: VibrationManager? = null

        fun getInstance(context: Context): VibrationManager {
            return instance ?: synchronized(this) {
                instance ?: VibrationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

