package com.ossama.qrvision.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QrClassifierTest {
    @Test fun classifiesHttpsAndHttpUrls() {
        assertEquals(QrContentType.URL, QrClassifier.classify("https://example.com/path"))
        assertEquals(QrContentType.URL, QrClassifier.classify("http://example.com"))
        assertEquals(QrContentType.URL, QrClassifier.classify("www.example.com/path"))
    }

    @Test fun classifiesWifiPayload() {
        assertEquals(QrContentType.WIFI, QrClassifier.classify("WIFI:T:WPA;S:Home;P:secret;;"))
    }

    @Test fun classifiesEmailAndPhone() {
        assertEquals(QrContentType.EMAIL, QrClassifier.classify("mailto:hello@example.com"))
        assertEquals(QrContentType.EMAIL, QrClassifier.classify("hello@example.com"))
        assertEquals(QrContentType.PHONE, QrClassifier.classify("+964 770 123 4567"))
        assertEquals(QrContentType.PHONE, QrClassifier.classify("tel:+9647701234567"))
    }

    @Test fun classifiesLocationSmsAndContact() {
        assertEquals(QrContentType.LOCATION, QrClassifier.classify("geo:32.6,44.0"))
        assertEquals(QrContentType.SMS, QrClassifier.classify("SMSTO:+9647701234567:Hello"))
        assertEquals(QrContentType.CONTACT, QrClassifier.classify("BEGIN:VCARD\nFN:Sample\nEND:VCARD"))
        assertEquals(QrContentType.CONTACT, QrClassifier.classify("MECARD:N:Sample;;"))
    }

    @Test fun distinguishesPlainTextAndUnknownSchemes() {
        assertEquals(QrContentType.TEXT, QrClassifier.classify("مرحبا بالعالم"))
        assertEquals(QrContentType.OTHER, QrClassifier.classify("bitcoin:abcdef"))
    }

    @Test fun handlesWhitespaceWithoutChangingInputStorage() {
        assertEquals(QrContentType.TEXT, QrClassifier.classify("   \n "))
    }
}
