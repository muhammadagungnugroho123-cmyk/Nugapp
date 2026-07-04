package com.example

import android.app.Application
import com.example.di.AppContainer

class ClockApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
