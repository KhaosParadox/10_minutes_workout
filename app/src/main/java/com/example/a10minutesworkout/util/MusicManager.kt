package com.example.a10minutesworkout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.a10minutesworkout.data.Track
import com.example.a10minutesworkout.R

class MusicManager(private val context: Context, private val onError: (String) -> Unit = {}) {
    private var player: MediaPlayer? = null
    private var playlist = emptyList<Track>()
    private var index = 0
    private var failures = 0
    private var muted = false
    private var shuffle = false
    private var ready = false
    private var wantsPlay = false
    private val handler = Handler(Looper.getMainLooper())

    fun setPlaylist(tracks: List<Track>, shuffle: Boolean) {
        stop()
        playlist = tracks.ifEmpty { listOf(Track(title = "Musique intégrée", uriString = "android.resource://${context.packageName}/${R.raw.background_music}", orderIndex = 0)) }
        this.shuffle = shuffle
        index = if (shuffle) playlist.indices.random() else 0
        failures = 0
    }

    fun play() {
        wantsPlay = true
        if (player == null) { failures = 0; load() } else if (ready) player?.start()
    }

    private fun load() {
        player?.release(); player = null; ready = false
        if (playlist.isEmpty() || !wantsPlay) return
        val candidate = MediaPlayer(); player = candidate
        try {
            candidate.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
            candidate.setDataSource(context, Uri.parse(playlist[index].uriString))
            candidate.setVolume(if (muted) 0f else 0.35f, if (muted) 0f else 0.35f)
            candidate.setOnPreparedListener {
                if (player === it) { ready = true; failures = 0; if (wantsPlay) it.start() }
            }
            candidate.setOnCompletionListener { nextIndex(); load() }
            candidate.setOnErrorListener { _, _, _ -> failed(); true }
            candidate.prepareAsync()
        } catch (e: Exception) { failed() }
    }

    private fun failed() {
        player?.release(); player = null; ready = false; failures++
        if (failures >= playlist.size) { wantsPlay = false; onError("Musique indisponible. Continue sans musique ou choisis un autre fichier dans les paramètres."); return }
        index = (index + 1) % playlist.size
        handler.post { load() }
    }

    private fun nextIndex() {
        index = if (shuffle && playlist.size > 1) playlist.indices.filter { it != index }.random() else (index + 1) % playlist.size
    }

    fun pause() { wantsPlay = false; if (ready) player?.pause() }
    fun stop() { wantsPlay = false; handler.removeCallbacksAndMessages(null); player?.release(); player = null; ready = false }
    fun setMute(mute: Boolean) { muted = mute; player?.setVolume(if (mute) 0f else 0.35f, if (mute) 0f else 0.35f) }
    fun release() = stop()
}
