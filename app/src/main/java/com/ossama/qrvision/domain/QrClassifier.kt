package com.ossama.qrvision.domain

import java.net.URI

/** A lightweight, deterministic classifier that never opens or executes the scanned payload. */
object QrClassifier {
    private val phonePattern = Regex("^\\+?[0-9][0-9\\s().-]{6,}$")
    private val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    private val schemePattern = Regex("^[A-Za-z][A-Za-z0-9+.-]*:")
    private val bareUrlPattern = Regex("^(?:www\\.)?[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+(?:[/:?#].*)?$", RegexOption.IGNORE_CASE)

    fun classify(raw: String): QrContentType {
        val value = raw.trim()
        if (value.isEmpty()) return QrContentType.TEXT
        if (value.startsWith("WIFI:", ignoreCase = true)) return QrContentType.WIFI
        if (value.startsWith("BEGIN:VCARD", ignoreCase = true) || value.startsWith("MECARD:", ignoreCase = true)) {
            return QrContentType.CONTACT
        }
        if (value.startsWith("geo:", ignoreCase = true) || value.startsWith("google.navigation:", ignoreCase = true)) {
            return QrContentType.LOCATION
        }
        if (value.startsWith("sms:", ignoreCase = true) || value.startsWith("smsto:", ignoreCase = true) ||
            value.startsWith("mmsto:", ignoreCase = true)) return QrContentType.SMS
        if (value.startsWith("mailto:", ignoreCase = true) || emailPattern.matches(value)) return QrContentType.EMAIL
        if (value.startsWith("tel:", ignoreCase = true) || phonePattern.matches(value)) return QrContentType.PHONE
        if (value.startsWith("https://", ignoreCase = true) || value.startsWith("http://", ignoreCase = true) || bareUrlPattern.matches(value)) {
            return QrContentType.URL
        }

        val scheme = try { URI(value).scheme } catch (_: Exception) { null }
        if (scheme != null || schemePattern.containsMatchIn(value)) return QrContentType.OTHER
        return QrContentType.TEXT
    }
}
