package com.example.a10minutesworkout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.a10minutesworkout.R

/**
 * SoundManager handles short sound effects like the workout bell.
 * It uses MediaPlayer for high reliability and consistent Audio Focus behavior (Ducking).
 */
class SoundManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    // Pre-define AudioAttributes for consistency
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    init {
        // Initialize AudioFocusRequest for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { /* No-op for short sounds */ }
                .build()
        }
    }

    /**
     * Plays the bell sound with audio ducking.
     */
    fun playBell() {
        try {
            // 1. Request Audio Focus to trigger ducking
            requestAudioFocus()

            // 2. Create and play the sound using MediaPlayer
            // We create a new instance each time to avoid state issues with simultaneous plays
            val mediaPlayer = MediaPlayer.create(context, R.raw.bell) ?: return

            mediaPlayer.setAudioAttributes(audioAttributes)

            mediaPlayer.setOnCompletionListener { mp ->
                // 3. Release focus once the sound is finished
                abandonAudioFocus()
                mp.release()
            }

            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing bell sound", e)
            // Ensure focus is released even on error
            abandonAudioFocus()
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun release() {
        // Nothing specific to release here as MediaPlayers are released in OnCompletion
    }
}
