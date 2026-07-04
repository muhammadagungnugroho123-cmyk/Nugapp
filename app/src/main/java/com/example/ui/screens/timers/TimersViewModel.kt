package com.example.ui.screens.timers

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimersViewModel(application: Application) : AndroidViewModel(application) {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _totalTime = MutableStateFlow(25 * 60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()

    private val _timeRemaining = MutableStateFlow(25 * 60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_timeRemaining.value <= 0) return
        _isRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _isRunning.value) {
                delay(1000)
                _timeRemaining.value -= 1
            }
            if (_timeRemaining.value == 0) {
                _isRunning.value = false
                showTimerFinishedNotification()
            }
        }
    }

    private fun showTimerFinishedNotification() {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "timer_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timers",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Timer Finished")
            .setContentText("Your timer is up!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()
            
        notificationManager.notify(2, notification)
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun stopTimer() {
        pauseTimer()
        _timeRemaining.value = _totalTime.value
    }

    fun setTotalTime(seconds: Int) {
        if (!_isRunning.value) {
            _totalTime.value = seconds
            _timeRemaining.value = seconds
        }
    }
}
