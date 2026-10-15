package com.ossama.qrvision

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ossama.qrvision.data.ScanEntry
import com.ossama.qrvision.ui.screens.HomeScreen
import com.ossama.qrvision.ui.screens.LibraryScreen
import com.ossama.qrvision.ui.screens.ResultScreen
import com.ossama.qrvision.ui.screens.ScannerScreen
import com.ossama.qrvision.ui.screens.SettingsScreen
import com.ossama.qrvision.ui.theme.QrVisionTheme

private enum class AppScreen { HOME, SCANNER, RESULT, SCAN_COMPLETE, HISTORY, FAVORITES, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 13)
        window.navigationBarColor = android.graphics.Color.rgb(8, 13, 13)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent { QrVisionApp() }
    }
}

@Composable
private fun QrVisionApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var cameraPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermissionGranted = granted
        if (!granted) viewModel.notifyMessage("إذن الكاميرا مرفوض. تگدر تختار صورة من المعرض أو تفعّل الإذن من إعدادات التطبيق.")
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.scanImage(uri) { found -> if (found) screen = if (state.settings.autoOpenResult) AppScreen.RESULT else AppScreen.SCAN_COMPLETE }
        }
    }

    LaunchedEffect(state.message) {
        val message = state.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    BackHandler(enabled = screen != AppScreen.HOME) {
        screen = when (screen) {
            AppScreen.RESULT, AppScreen.SCAN_COMPLETE, AppScreen.SCANNER, AppScreen.HISTORY, AppScreen.FAVORITES, AppScreen.SETTINGS -> AppScreen.HOME
            else -> AppScreen.HOME
        }
    }

    QrVisionTheme(settings = state.settings) {
        androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val showBottomNavigation = screen in setOf(AppScreen.HOME, AppScreen.HISTORY, AppScreen.FAVORITES, AppScreen.SETTINGS)
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showBottomNavigation) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                selected = screen == AppScreen.HOME,
                                onClick = { screen = AppScreen.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("الرئيسية") }
                            )
                            NavigationBarItem(
                                selected = screen == AppScreen.HISTORY,
                                onClick = { screen = AppScreen.HISTORY },
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text("السجل") }
                            )
                            NavigationBarItem(
                                selected = screen == AppScreen.FAVORITES,
                                onClick = { screen = AppScreen.FAVORITES },
                                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                                label = { Text("المفضلة") }
                            )
                            NavigationBarItem(
                                selected = screen == AppScreen.SETTINGS,
                                onClick = { screen = AppScreen.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("الإعدادات") }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    when (screen) {
                        AppScreen.HOME -> HomeScreen(
                            history = state.history,
                            onScan = {
                                screen = AppScreen.SCANNER
                                if (!cameraPermissionGranted) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onGallery = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onHistory = { screen = AppScreen.HISTORY },
                            onFavorites = { screen = AppScreen.FAVORITES },
                            onSettings = { screen = AppScreen.SETTINGS },
                            onOpenEntry = { viewModel.openEntry(it); screen = AppScreen.RESULT }
                        )
                        AppScreen.SCANNER -> ScannerScreen(
                            hasPermission = cameraPermissionGranted,
                            onRequestPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            onOpenPermissionSettings = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                                } catch (_: Exception) {
                                    viewModel.notifyMessage("افتح إعدادات أندرويد ثم التطبيقات ثم QR Vision وفعّل الكاميرا.")
                                }
                            },
                            onBack = { screen = AppScreen.HOME },
                            onDetected = { raw ->
                                viewModel.processScan(raw)
                                screen = if (state.settings.autoOpenResult) AppScreen.RESULT else AppScreen.SCAN_COMPLETE
                            },
                            onCameraError = { viewModel.notifyMessage("تعذّر تشغيل الكاميرا. تأكد من أن تطبيقاً آخر ما يستخدمها وجرب مرة ثانية.") }
                        )
                        AppScreen.RESULT -> ResultScreen(
                            entry = state.currentResult,
                            isFavorite = state.currentResult?.let { result -> state.favorites.any { it.rawValue == result.rawValue } } == true,
                            onBack = { screen = AppScreen.HOME },
                            onCopy = { copyText(context, it); viewModel.notifyMessage("تم نسخ المحتوى.") },
                            onShare = { shareText(context, it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it); viewModel.notifyMessage("تم تحديث المفضلة.") },
                            onScanAgain = {
                                screen = AppScreen.SCANNER
                                if (!cameraPermissionGranted) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onHistory = { screen = AppScreen.HISTORY }
                        )
                        AppScreen.SCAN_COMPLETE -> ScanCompleteScreen(
                            onViewResult = { screen = AppScreen.RESULT },
                            onScanAgain = {
                                screen = AppScreen.SCANNER
                                if (!cameraPermissionGranted) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onHome = { screen = AppScreen.HOME }
                        )
                        AppScreen.HISTORY -> LibraryScreen(
                            title = "سجل القراءة", items = state.history, isFavorites = false,
                            onBack = { screen = AppScreen.HOME },
                            onOpenEntry = { viewModel.openEntry(it); screen = AppScreen.RESULT },
                            onToggleFavorite = { viewModel.toggleFavorite(it); viewModel.notifyMessage("تم تحديث المفضلة.") },
                            onDelete = { viewModel.deleteHistory(it) }, onClear = { viewModel.clearHistory() }
                        )
                        AppScreen.FAVORITES -> LibraryScreen(
                            title = "المفضلة", items = state.favorites, isFavorites = true,
                            onBack = { screen = AppScreen.HOME },
                            onOpenEntry = { viewModel.openEntry(it); screen = AppScreen.RESULT },
                            onToggleFavorite = { viewModel.removeFavorite(it) },
                            onDelete = { viewModel.removeFavorite(it) }, onClear = { viewModel.clearFavorites() }
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            settings = state.settings,
                            onSettingsChange = { viewModel.updateSettings(it) },
                            onBack = { screen = AppScreen.HOME },
                            onHistory = { screen = AppScreen.HISTORY },
                            onFavorites = { screen = AppScreen.FAVORITES }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanCompleteScreen(onViewResult: () -> Unit, onScanAgain: () -> Unit, onHome: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 18.dp).fillMaxWidth().size(62.dp))
        Text("تمت القراءة بنجاح", style = MaterialTheme.typography.headlineSmall)
        Text("إعداد العرض التلقائي مطفّي. اختار متى تريد تشوف التفاصيل.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
        androidx.compose.material3.Button(onClick = onViewResult, modifier = Modifier.fillMaxWidth()) { Text("عرض النتيجة") }
        androidx.compose.material3.OutlinedButton(onClick = onScanAgain, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("مسح رمز آخر") }
        androidx.compose.material3.TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("الرئيسية") }
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("QR Vision", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة نتيجة QR"))
}
