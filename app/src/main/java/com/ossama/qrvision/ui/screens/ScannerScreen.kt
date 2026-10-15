package com.ossama.qrvision.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScannerScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onBack: () -> Unit,
    onDetected: (String) -> Unit,
    onCameraError: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Column(Modifier.weight(1f)) {
                Text("مسح QR", style = MaterialTheme.typography.titleLarge)
                Text("وجّه الرمز داخل الإطار", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
        }
        if (hasPermission) {
            CameraPreview(onDetected = onDetected, onCameraError = onCameraError, modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                        Text("نحتاج إذن الكاميرا", style = MaterialTheme.typography.titleLarge)
                        Text("الإذن يُستخدم فقط وقت المسح. تگدر تختار صورة من المعرض بدون ما تمنح الإذن.", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) { Text("السماح بالكاميرا") }
                        OutlinedButton(onClick = onOpenPermissionSettings, modifier = Modifier.fillMaxWidth()) { Text("فتح إعدادات التطبيق") }
                        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("رجوع للرئيسية") }
                    }
                }
            }
        }
        Text("المعالجة تصير على جهازك، وما نرفع الصورة أو محتوى الرمز إلى أي خادم.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun CameraPreview(onDetected: (String) -> Unit, onCameraError: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var cameraError by remember { mutableStateOf(false) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    DisposableEffect(lifecycleOwner, previewView) {
        val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        val barcodeScanner = BarcodeScanning.getClient(options)
        val handled = AtomicBoolean(false)
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var disposed = false

        providerFuture.addListener({
            if (disposed) return@addListener
            try {
                provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                    } else {
                        try {
                            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(input)
                                .addOnSuccessListener(mainExecutor) { barcodes ->
                                    val value = barcodes.firstNotNullOfOrNull {
                                        it.rawValue?.takeIf { raw -> raw.isNotBlank() && it.format == Barcode.FORMAT_QR_CODE }
                                    }
                                    if (value != null && handled.compareAndSet(false, true)) onDetected(value)
                                }
                                .addOnFailureListener(mainExecutor) { /* Keep scanning; transient frame failures are expected. */ }
                                .addOnCompleteListener { imageProxy.close() }
                        } catch (_: Exception) {
                            imageProxy.close()
                        }
                    }
                }
                camera = provider?.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                loading = false
            } catch (_: Exception) {
                loading = false
                cameraError = true
                onCameraError()
            }
        }, mainExecutor)

        onDispose {
            disposed = true
            try { provider?.unbindAll() } catch (_: Exception) { }
            try { barcodeScanner.close() } catch (_: Exception) { }
            analysisExecutor.shutdown()
            camera = null
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(28.dp)).background(Color.Black)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.16f), Color.Transparent, Color.Black.copy(alpha = 0.38f)))
            )
        )
        if (!cameraError) {
            Box(Modifier.align(Alignment.Center).size(255.dp), contentAlignment = Alignment.Center) { ScanFrame() }
        } else {
            Card(
                modifier = Modifier.align(Alignment.Center).padding(22.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171F1C))
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تعذّر تشغيل الكاميرا", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("ارجع للشاشة الرئيسية واختار صورة من المعرض، أو جرّب مرة ثانية بعد إغلاق تطبيقات الكاميرا الأخرى.", color = Color(0xFFD1E6DD), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (hasFlash && !cameraError) {
            IconButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    camera?.cameraControl?.enableTorch(flashEnabled)
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.46f))
            ) {
                Icon(if (flashEnabled) Icons.Default.FlashOff else Icons.Default.FlashOn, contentDescription = if (flashEnabled) "إطفاء الفلاش" else "تشغيل الفلاش", tint = Color.White)
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading) CircularProgressIndicator(color = Color(0xFF00E6A9), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(if (loading) "تهيئة الكاميرا…" else "خلّي رمز QR داخل الإطار", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Text("راح تنقري النتيجة مرة وحدة", color = Color(0xFFD1E6DD), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ScanFrame() {
    val transition = rememberInfiniteTransition(label = "scan-line")
    val progress by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "scan-progress"
    )
    Canvas(Modifier.fillMaxSize()) {
        val stroke = 5.dp.toPx()
        val length = 33.dp.toPx()
        val inset = stroke / 2f
        val right = size.width - inset
        val bottom = size.height - inset
        val color = Color(0xFF00E6A9)
        // Four crisp scanning brackets rather than a heavy enclosing box.
        val segments = listOf(
            Offset(inset, inset) to Offset(inset + length, inset), Offset(inset, inset) to Offset(inset, inset + length),
            Offset(right - length, inset) to Offset(right, inset), Offset(right, inset) to Offset(right, inset + length),
            Offset(inset, bottom) to Offset(inset + length, bottom), Offset(inset, bottom - length) to Offset(inset, bottom),
            Offset(right - length, bottom) to Offset(right, bottom), Offset(right, bottom - length) to Offset(right, bottom)
        )
        segments.forEach { (start, end) -> drawLine(color, start, end, strokeWidth = stroke) }
        val y = size.height * progress
        drawLine(color.copy(alpha = 0.82f), Offset(12.dp.toPx(), y), Offset(size.width - 12.dp.toPx(), y), strokeWidth = 2.dp.toPx())
    }
}
