package com.ossama.qrvision.domain

/** Content categories inferred from the original payload; no payload is executed automatically. */
enum class QrContentType(val key: String, val label: String, val symbol: String) {
    URL("url", "رابط موقع", "↗"),
    TEXT("text", "نص عادي", "Aa"),
    PHONE("phone", "رقم هاتف", "☎"),
    EMAIL("email", "بريد إلكتروني", "✉"),
    WIFI("wifi", "شبكة Wi‑Fi", "⌁"),
    LOCATION("location", "موقع جغرافي", "⌖"),
    SMS("sms", "رسالة SMS", "✉"),
    CONTACT("contact", "بطاقة جهة اتصال", "♙"),
    OTHER("other", "محتوى آخر", "◇");

    companion object {
        fun fromKey(key: String): QrContentType = entries.firstOrNull { it.key == key } ?: OTHER
    }
}
