package com.ossama.qrvision.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ossama.qrvision.data.ScanEntry
import com.ossama.qrvision.domain.QrContentType

@Composable
fun LibraryScreen(
    title: String,
    items: List<ScanEntry>,
    isFavorites: Boolean,
    onBack: () -> Unit,
    onOpenEntry: (ScanEntry) -> Unit,
    onToggleFavorite: (ScanEntry) -> Unit,
    onDelete: (ScanEntry) -> Unit,
    onClear: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val typeOptions = listOf(
        "all" to "الكل", "url" to "روابط", "text" to "نصوص", "phone" to "هاتف",
        "email" to "بريد", "wifi" to "Wi‑Fi", "location" to "موقع", "sms" to "SMS", "contact" to "جهات", "other" to "أخرى"
    )
    val filtered = items.filter { entry ->
        val matchesQuery = query.isBlank() || entry.rawValue.contains(query, ignoreCase = true) || entry.type.label.contains(query, ignoreCase = true)
        val matchesType = selectedType == "all" || entry.type.key == selectedType
        matchesQuery && matchesType
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع") }
            Icon(if (isFavorites) Icons.Default.Favorite else Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
            if (items.isNotEmpty()) {
                IconButton(onClick = { showClearConfirmation = true }) { Icon(Icons.Default.Delete, contentDescription = if (isFavorites) "مسح المفضلة" else "مسح السجل") }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("ابحث داخل النتائج…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            typeOptions.forEach { (key, label) ->
                FilterChip(
                    selected = selectedType == key,
                    onClick = { selectedType = key },
                    label = { Text(label) }
                )
            }
        }
        if (filtered.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(if (isFavorites) Icons.Default.Favorite else Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                Text(if (items.isEmpty()) if (isFavorites) "المفضلة فارغة" else "السجل فارغ" else "ماكو نتائج مطابقة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                Text(if (items.isEmpty()) "النتائج اللي تقراها راح تظهر هنا." else "جرّب كلمة ثانية أو غيّر نوع المحتوى.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenEntry(entry) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.type.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(formatDate(entry.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(entry.rawValue, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                Text("عرض التفاصيل", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                if (isFavorites) {
                                    IconButton(onClick = { onToggleFavorite(entry) }) { Icon(Icons.Default.Favorite, contentDescription = "إزالة من المفضلة", tint = MaterialTheme.colorScheme.primary) }
                                } else {
                                    IconButton(onClick = { onToggleFavorite(entry) }) { Icon(Icons.Default.Favorite, contentDescription = "تبديل المفضلة", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Default.Delete, contentDescription = "حذف من السجل", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(if (isFavorites) "مسح كل المفضلة؟" else "مسح سجل القراءة؟") },
            text = { Text(if (isFavorites) "راح تنحذف العناصر المحفوظة بالمفضلة فقط." else "راح تنحذف نتائج السجل. عناصر المفضلة تبقى محفوظة.") },
            confirmButton = { TextButton(onClick = { showClearConfirmation = false; onClear() }) { Text("مسح الكل") } },
            dismissButton = { TextButton(onClick = { showClearConfirmation = false }) { Text("إلغاء") } }
        )
    }
}
