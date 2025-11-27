package com.mediguide.firstaid.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.mediguide.firstaid.R
import com.mediguide.firstaid.utils.UserPreferencesManager

/**
 * Manages sound effects throughout the app
 * Respects user's sound preference setting
 */
class SoundManager(private val context: Context) {

    private val preferencesManager = UserPreferencesManager(context)
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        CLICK,
        SUCCESS,
        ERROR,
        ALERT,
        NOTIFICATION
    }

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sound effects (we'll use system sounds for now since we don't have custom sound files)
        // In a real implementation, you would load custom sounds from res/raw
        // For now, this structure is ready but won't play actual sounds
    }

    /**
     * Play a sound effect if sound is enabled in preferences
     */
    fun playSound(soundType: SoundType) {
        if (!preferencesManager.isSoundEnabled) {
            return
        }

        soundMap[soundType]?.let { soundId ->
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play click sound
     */
    fun playClick() {
        playSound(SoundType.CLICK)
    }

    /**
     * Play success sound
     */
    fun playSuccess() {
        playSound(SoundType.SUCCESS)
    }

    /**
     * Play error sound
     */
    fun playError() {
        playSound(SoundType.ERROR)
    }

    /**
     * Play alert sound
     */
    fun playAlert() {
        playSound(SoundType.ALERT)
    }

    /**
     * Release resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

