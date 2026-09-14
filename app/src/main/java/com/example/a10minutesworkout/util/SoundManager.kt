package com.example.a10minutesworkout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.a10minutesworkout.R

class SoundManager(context: Context) {
    private var ready = false
    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()).build()
    private val bell = pool.load(context, R.raw.bell, 1)
    init { pool.setOnLoadCompleteListener { _, _, status -> ready = status == 0 } }
    fun playBell() { if (ready) pool.play(bell, 0.7f, 0.7f, 1, 0, 1f) }
    fun release() { ready = false; pool.release() }
}
