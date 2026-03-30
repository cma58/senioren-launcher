package com.seniorenlauncher.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorenlauncher.data.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    fun playStation(station: RadioStation) {
        viewModelScope.launch {
            if (_currentStation.value == station && _isPlaying.value) {
                pause()
                return@launch
            }

            if (_currentStation.value == station && !_isPlaying.value && mediaPlayer != null) {
                resume()
                return@launch
            }

            stopAndRelease()
            _currentStation.value = station
            _isLoading.value = true
            _hasError.value = false

            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    withContext(Dispatchers.IO) {
                        setDataSource(station.url)
                    }
                    setOnPreparedListener {
                        it.start()
                        _isPlaying.value = true
                        _isLoading.value = false
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("RadioVM", "MediaPlayer Error: $what, $extra")
                        _hasError.value = true
                        _isLoading.value = false
                        _isPlaying.value = false
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                Log.e("RadioVM", "Failed to setup radio", e)
                _hasError.value = true
                _isLoading.value = false
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            stopAndRelease()
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            _currentStation.value?.let { playStation(it) }
        }
    }

    private fun stopAndRelease() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopAndRelease()
    }
}
