package com.ossama.qrvision.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ossama.qrvision.BuildConfig
import com.ossama.qrvision.data.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onFavorites: () -> Unit
) {
    var showPrivacy by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Text("الإعدادات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("المظهر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("system" to "النظام", "dark" to "داكن", "light" to "فاتح").forEach { (key, label) ->
                    FilterChip(selected = settings.themeMode == key, onClick = { onSettingsChange(settings.copy(themeMode = key)) }, label = { Text(label) })
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("سلوك المسح", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingSwitch("اهتزاز عند اكتشاف الرمز", "تنبيه اهتزاز خفيف بعد القراءة.", settings.vibrateOnScan) { onSettingsChange(settings.copy(vibrateOnScan = it)) }
            SettingSwitch("صوت تأكيد اختياري", "نغمة قصيرة عند نجاح القراءة.", settings.soundOnScan) { onSettingsChange(settings.copy(soundOnScan = it)) }
            SettingSwitch("عرض النتيجة تلقائياً", "إذا عطّلتها تظهر شاشة تأكيد قبل تفاصيل النتيجة.", settings.autoOpenResult) { onSettingsChange(settings.copy(autoOpenResult = it)) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("إدارة البيانات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("إدارة سجل القراءة") }
            OutlinedButton(onClick = onFavorites, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("إدارة المفضلة") }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("الخصوصية والمعلومات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(onClick = { showPrivacy = true }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("سياسة الخصوصية", fontWeight = FontWeight.SemiBold)
                        Text("شلون يتعامل التطبيق ويا بياناتك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Card(onClick = { showAbout = true }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("حول التطبيق", fontWeight = FontWeight.SemiBold)
                        Text("QR Vision · الإصدار ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.size(8.dp))
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("سياسة الخصوصية") },
            text = { Text("تتم قراءة رموز QR داخل الجهاز باستخدام نموذج ML Kit المضمّن. لا يطلب التطبيق حساباً أو تسجيل دخول، ولا يرسل صور المعرض أو محتوى الرموز إلى خادم، ولا يحتوي على إعلانات أو أدوات تتبّع. تُحفظ النتائج والإعدادات محلياً على جهازك، وتبقى مسؤولاً عن قرار فتح الروابط أو تمرير المحتوى إلى تطبيق آخر. حذف التطبيق يزيل بياناته المحلية عادةً.") },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("فهمت") } }
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("QR Vision") },
            text = { Text("قارئ QR الذكي\nالإصدار ${BuildConfig.VERSION_NAME}\nمعالجة محلية، واجهة عربية، وسجل ومفضلة على الجهاز.") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
