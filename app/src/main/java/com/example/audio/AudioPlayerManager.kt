package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack.asStateFlow()

    private val _currentTitle = MutableStateFlow<String>("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun playAudio(url: String, trackTitle: String = "") {
        try {
            if (_isPlaying.value && _currentTrack.value == url) {
                // Pause current playing audio
                mediaPlayer?.pause()
                _isPlaying.value = false
                return
            }

            if (!_isPlaying.value && _currentTrack.value == url && mediaPlayer != null) {
                // Resume already prepared audio
                mediaPlayer?.start()
                _isPlaying.value = true
                return
            }

            stop()

            _isLoading.value = true
            _currentTrack.value = url
            _currentTitle.value = trackTitle

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    _isLoading.value = false
                    mp.start()
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _isLoading.value = false
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    _isLoading.value = false
                    _currentTrack.value = null
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (_: Exception) {
            _isLoading.value = false
            _isPlaying.value = false
        }
    }

    fun playAdhan() {
        val adhanUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Adhan_Dubai_UAE_(%D8%A3%D8%B0%D8%A7%D9%86_%D8%AF%D8%A8%D9%8A_%D8%A7%D9%84%D8%A5%D9%85%D8%A7%D8%B1%D8%A7%D8%AA).mp3"
        playAudio(adhanUrl, "أذان الحرم")
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _isLoading.value = false
        _currentTrack.value = null
        _currentTitle.value = ""
    }

    fun release() {
        stop()
    }
}
