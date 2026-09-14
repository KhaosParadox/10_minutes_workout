package com.example.a10minutesworkout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.a10minutesworkout.data.Track

class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false
    private var playlist: List<Track> = emptyList()
    private var currentIndex = 0
    private var isShuffle = false

    fun setPlaylist(tracks: List<Track>, shuffle: Boolean) {
        this.playlist = tracks
        this.isShuffle = shuffle
        this.currentIndex = if (shuffle && tracks.isNotEmpty()) (tracks.indices).random() else 0
        Log.d("MusicManager", "Playlist updated: ${tracks.size} tracks, shuffle=$shuffle")
    }

    fun play() {
        if (mediaPlayer?.isPlaying == true) return
        
        if (mediaPlayer == null) {
            startNextTrack()
        } else {
            mediaPlayer?.start()
        }
    }

    private fun startNextTrack() {
        mediaPlayer?.release()
        mediaPlayer = null

        if (playlist.isEmpty()) {
            Log.w("MusicManager", "Playlist is empty, nothing to play")
            return
        }

        val track = playlist.getOrNull(currentIndex) ?: return

        try {
            val uri = Uri.parse(track.uriString)
            Log.d("MusicManager", "Attempting to play: ${track.title} (URI: ${track.uriString})")
            
            if (uri.scheme == "android.resource") {
                val resId = uri.pathSegments.lastOrNull()?.toIntOrNull()
                if (resId != null) {
                    mediaPlayer = MediaPlayer.create(context, resId)
                }
            } else {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, uri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    prepare()
                }
            }

            mediaPlayer?.apply {
                val volume = if (isMuted) 0f else 1f
                setVolume(volume, volume)
                
                setOnCompletionListener {
                    if (isShuffle && playlist.size > 1) {
                        currentIndex = (playlist.indices).random()
                    } else {
                        currentIndex = (currentIndex + 1) % playlist.size
                    }
                    startNextTrack()
                }
                
                setOnErrorListener { _, _, _ ->
                    currentIndex = (currentIndex + 1) % playlist.size
                    startNextTrack()
                    true
                }
                
                start()
            }
        } catch (e: Exception) {
            Log.e("MusicManager", "Error playing ${track.title}", e)
            if (playlist.size > 1) {
                currentIndex = (currentIndex + 1) % playlist.size
                startNextTrack()
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setMute(mute: Boolean) {
        isMuted = mute
        val volume = if (mute) 0f else 1f
        mediaPlayer?.setVolume(volume, volume)
    }

    fun isMuted() = isMuted

    fun release() {
        stop()
    }
}
