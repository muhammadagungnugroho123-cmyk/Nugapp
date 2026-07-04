package com.example.ui.screens.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps: StateFlow<List<Long>> = _laps.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L
    private var timeAtPause = 0L

    fun toggleStopwatch() {
        if (_isRunning.value) {
            pauseStopwatch()
        } else {
            startStopwatch()
        }
    }

    private fun startStopwatch() {
        _isRunning.value = true
        startTime = System.currentTimeMillis() - timeAtPause
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(10)
                _elapsedTime.value = System.currentTimeMillis() - startTime
            }
        }
    }

    private fun pauseStopwatch() {
        _isRunning.value = false
        timeAtPause = System.currentTimeMillis() - startTime
        timerJob?.cancel()
    }

    fun lapOrReset() {
        if (_isRunning.value) {
            // Lap
            _laps.value = _laps.value + _elapsedTime.value
        } else {
            // Reset
            timeAtPause = 0L
            _elapsedTime.value = 0L
            _laps.value = emptyList()
        }
    }

    fun resetStopwatch() {
        pauseStopwatch()
        timeAtPause = 0L
        _elapsedTime.value = 0L
        _laps.value = emptyList()
    }
}
