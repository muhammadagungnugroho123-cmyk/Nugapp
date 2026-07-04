package com.example.ui.screens.worldclock

data class WorldCity(
    val name: String,
    val country: String,
    val timeZoneId: String,
    val lat: Float = 0f,
    val lon: Float = 0f,
    val flag: String = ""
)
