package com.ossama.qrvision.data

import com.ossama.qrvision.domain.QrContentType

data class ScanEntry(
    val id: String,
    val rawValue: String,
    val type: QrContentType,
    val timestamp: Long
)
