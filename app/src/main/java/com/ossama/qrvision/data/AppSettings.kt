package com.ossama.qrvision.data

data class AppSettings(
    val themeMode: String = "system",
    val vibrateOnScan: Boolean = true,
    val soundOnScan: Boolean = false,
    val autoOpenResult: Boolean = true
)
