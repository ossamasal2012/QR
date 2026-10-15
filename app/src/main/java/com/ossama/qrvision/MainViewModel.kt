package com.ossama.qrvision

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ossama.qrvision.data.AppSettings
import com.ossama.qrvision.data.QrRepository
import com.ossama.qrvision.data.ScanEntry
import com.ossama.qrvision.data.qrVisionDataStore
import com.ossama.qrvision.domain.QrClassifier
import com.ossama.qrvision.domain.QrContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class AppUiState(
    val history: List<ScanEntry> = emptyList(),
    val favorites: List<ScanEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val currentResult: ScanEntry? = null,
    val message: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QrRepository(application.qrVisionDataStore)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.history, repository.favorites, repository.settings) { history, favorites, settings ->
                Triple(history, favorites, settings)
            }.collect { data ->
                _state.update { it.copy(history = data.first, favorites = data.second, settings = data.third) }
            }
        }
    }

    fun processScan(rawValue: String) {
        if (rawValue.isBlank()) {
            notifyMessage("الرمز ما يحتوي على نص قابل للقراءة.")
            return
        }
        val entry = ScanEntry(
            id = UUID.randomUUID().toString(),
            rawValue = rawValue,
            type = QrClassifier.classify(rawValue),
            timestamp = System.currentTimeMillis()
        )
        _state.update { it.copy(currentResult = entry, message = null) }
        runFeedback(_state.value.settings)
        viewModelScope.launch {
            try {
                repository.addHistory(entry)
            } catch (_: Exception) {
                notifyMessage("تعذّر حفظ النتيجة بالسجل، بس القراءة تمت.")
            }
        }
    }

    fun openEntry(entry: ScanEntry) {
        _state.update { it.copy(currentResult = entry) }
    }

    fun toggleFavorite(entry: ScanEntry) {
        viewModelScope.launch {
            try { repository.toggleFavorite(entry) }
            catch (_: Exception) { notifyMessage("تعذّر تحديث المفضلة.") }
        }
    }

    fun deleteHistory(entry: ScanEntry) {
        viewModelScope.launch {
            try { repository.removeHistory(entry.id) }
            catch (_: Exception) { notifyMessage("تعذّر حذف النتيجة.") }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try { repository.clearHistory(); notifyMessage("تم مسح سجل القراءة.") }
            catch (_: Exception) { notifyMessage("تعذّر مسح السجل.") }
        }
    }

    fun removeFavorite(entry: ScanEntry) {
        viewModelScope.launch {
            try { repository.removeFavorite(entry.id) }
            catch (_: Exception) { notifyMessage("تعذّر إزالة العنصر من المفضلة.") }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            try { repository.clearFavorites(); notifyMessage("تم مسح المفضلة.") }
            catch (_: Exception) { notifyMessage("تعذّر مسح المفضلة.") }
        }
    }

    fun updateSettings(settings: AppSettings) {
        _state.update { it.copy(settings = settings) }
        viewModelScope.launch {
            try { repository.saveSettings(settings) }
            catch (_: Exception) { notifyMessage("تعذّر حفظ الإعدادات.") }
        }
    }

    fun notifyMessage(message: String) { _state.update { it.copy(message = message) } }
    fun clearMessage() { _state.update { it.copy(message = null) } }

    fun scanImage(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(message = "جارٍ فحص الصورة محلياً…") }
            val result = withContext(Dispatchers.IO) {
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)
                try {
                    val image = InputImage.fromFilePath(getApplication<Application>(), uri)
                    val barcode = Tasks.await(scanner.process(image))
                        .firstOrNull { it.format == Barcode.FORMAT_QR_CODE && !it.rawValue.isNullOrBlank() }
                    ImageScanResult.FoundOrEmpty(barcode?.rawValue)
                } catch (_: Exception) {
                    ImageScanResult.Failure
                } finally {
                    scanner.close()
                }
            }
            when (result) {
                is ImageScanResult.FoundOrEmpty -> {
                    val raw = result.rawValue
                    if (raw.isNullOrBlank()) {
                        notifyMessage("ما لگيت رمز QR واضح بهالصورة.")
                        onComplete(false)
                    } else {
                        processScan(raw)
                        onComplete(true)
                    }
                }
                ImageScanResult.Failure -> {
                    notifyMessage("ما گدرت أقرأ الصورة. جرّب صورة أوضح أو صيغة ثانية.")
                    onComplete(false)
                }
            }
        }
    }

    private fun runFeedback(settings: AppSettings) {
        val context = getApplication<Application>()
        if (settings.vibrateOnScan) {
            try {
                val vibrator = context.getSystemService(Vibrator::class.java)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(90L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(90L)
                    }
                }
            } catch (_: Exception) { }
        }
        if (settings.soundOnScan) {
            try {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
                tone.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 400L)
            } catch (_: Exception) { }
        }
    }

    private sealed interface ImageScanResult {
        data class FoundOrEmpty(val rawValue: String?) : ImageScanResult
        data object Failure : ImageScanResult
    }
}
