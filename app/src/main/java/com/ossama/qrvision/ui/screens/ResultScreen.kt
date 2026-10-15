package com.ossama.qrvision.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ossama.qrvision.data.ScanEntry

@Composable
fun ResultScreen(
    entry: ScanEntry?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: (ScanEntry) -> Unit,
    onScanAgain: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    var showOpenConfirmation by remember(entry?.id) { mutableStateOf(false) }
    if (entry == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ماكو نتيجة معروضة حالياً")
            Button(onClick = onBack) { Text("رجوع") }
        }
        return
    }

    val actionable = entry.type.key in setOf("url", "phone", "email", "location", "sms")
    val isInsecureHttp = entry.rawValue.trim().startsWith("http://", ignoreCase = true)

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Text("نتيجة المسح", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onScanAgain) { Icon(Icons.Default.QrCodeScanner, contentDescription = "مسح رمز آخر") }
        }
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.type.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(formatDate(entry.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Text(entry.type.symbol, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                if (isInsecureHttp) {
                    Text("تنبيه: هذا الرابط يستخدم HTTP غير المشفّر. تأكد من العنوان قبل فتحه.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                SelectionContainer {
                    Text(entry.rawValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
                Text("القراءة انحفظت محلياً بالسجل.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onCopy(entry.rawValue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("نسخ المحتوى")
            }
            OutlinedButton(onClick = { onShare(entry.rawValue) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("مشاركة النتيجة")
            }
            OutlinedButton(onClick = { onToggleFavorite(entry) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(8.dp))
                Text(if (isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة")
            }
            if (actionable) {
                Button(onClick = { showOpenConfirmation = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("فتح باستخدام تطبيق آخر")
                }
            }
            TextButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("الذهاب إلى سجل القراءة") }
        }
        Text(
            "تنبيه أمان: نجاح القراءة ما يعني أن المحتوى موثوق. لا تفتح رابطاً ما تعرفه.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.size(6.dp))
    }

    if (showOpenConfirmation) {
        AlertDialog(
            onDismissRequest = { showOpenConfirmation = false },
            title = { Text("تريد تفتح المحتوى؟") },
            text = { Text(if (isInsecureHttp) "الرابط غير مشفّر. تأكد من العنوان، وراح يفتح بتطبيق مناسب بعد موافقتك." else "راح يتم تمرير المحتوى إلى تطبيق أندرويد مناسب. لا تستمر إلا إذا تثق بالمحتوى.") },
            confirmButton = {
                TextButton(onClick = {
                    showOpenConfirmation = false
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, actionUri(entry)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        android.widget.Toast.makeText(context, "ماكو تطبيق مناسب لفتح هالمحتوى.", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (_: Exception) {
                        android.widget.Toast.makeText(context, "ما گدرت أفتح هالمحتوى.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) { Text("فتح") }
            },
            dismissButton = { TextButton(onClick = { showOpenConfirmation = false }) { Text("إلغاء") } }
        )
    }
}


private fun actionUri(entry: ScanEntry): Uri {
    val value = entry.rawValue.trim()
    return when (entry.type.key) {
        "url" -> Uri.parse(if (Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(value)) value else "https://$value")
        "phone" -> Uri.parse(if (value.startsWith("tel:", ignoreCase = true)) value else "tel:${value.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }}")
        "email" -> Uri.parse(if (value.startsWith("mailto:", ignoreCase = true)) value else "mailto:$value")
        else -> Uri.parse(value)
    }
}
