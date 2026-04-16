package com.obrynex.studyguard.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.data.db.StudySession
import com.obrynex.studyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session : StudySession,
    onBack  : () -> Unit,
    onDelete: (StudySession) -> Unit
) {
    val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextMuted)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onDelete(session); onBack() },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("حذف", color = AccentRed, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Subject ───────────────────────────────────────────
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(session.subject, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Medium)
                Text(session.dateKey, color = TextMuted, fontSize = 13.sp)
            }

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // ── Stats ─────────────────────────────────────────────
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                StatRow(label = "المدة", value = "${session.durationMin} دقيقة")
                HorizontalDivider(color = Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                StatRow(label = "البداية", value = timeFmt.format(Date(session.startMs)))
                HorizontalDivider(color = Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                StatRow(label = "النهاية", value = timeFmt.format(Date(session.endMs)))
                HorizontalDivider(color = Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                StatRow(label = "التاريخ", value = session.dateKey)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            fontFamily = if (label == "المدة") FontFamily.Monospace else FontFamily.Default)
    }
}
