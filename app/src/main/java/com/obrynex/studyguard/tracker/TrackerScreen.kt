package com.obrynex.studyguard.tracker

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.obrynex.studyguard.data.db.StudySession
import com.obrynex.studyguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerScreen(vm: TrackerViewModel, onSessionClick: (StudySession) -> Unit) {
    val s by vm.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Text("المذاكرة", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Medium)
            Text(
                SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date()),
                color = TextMuted, fontSize = 13.sp
            )
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Today's stats ─────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            // Today's minutes — big number
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${s.todayMinutes}",
                    color      = TextPrimary,
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Text("دقيقة اليوم", color = TextMuted, fontSize = 12.sp)
            }
            // Streak + goal
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${s.streak}",
                        color = if (s.streak > 0) AccentAmber else TextMuted,
                        fontSize = 22.sp, fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("يوم متواصل", color = TextMuted, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${s.goalMinutes}",
                        color = TextMuted, fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("الهدف", color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        // ── Progress bar ──────────────────────────────────────────
        val pct = (s.todayMinutes.toFloat() / s.goalMinutes.toFloat()).coerceIn(0f, 1f)
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LinearProgressIndicator(
                progress   = { pct },
                modifier   = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color      = if (pct >= 1f) AccentGreen else Accent,
                trackColor = Surface2,
                strokeCap  = StrokeCap.Round
            )
            if (pct >= 1f) {
                Text("وصلت لهدفك اليوم", color = AccentGreen, fontSize = 11.sp)
            } else {
                Text(
                    "${s.goalMinutes - s.todayMinutes} دقيقة متبقية",
                    color = TextMuted, fontSize = 11.sp
                )
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(top = 24.dp))

        // ── Subject + Timer ───────────────────────────────────────
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value         = s.subject,
                onValueChange = vm::onSubjectChanged,
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("المادة", color = TextMuted, fontSize = 13.sp) },
                singleLine    = true,
                enabled       = !s.isRunning,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Accent.copy(alpha = 0.7f),
                    unfocusedBorderColor    = Surface2,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp)
            )

            // Timer
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedContent(vm.formatElapsed(s.elapsedMs), label = "timer") { t ->
                    Text(
                        t,
                        color      = if (s.isRunning) TextPrimary else TextMuted,
                        fontSize   = 52.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (s.isRunning) AccentRed.copy(alpha = 0.12f) else Accent.copy(alpha = 0.12f))
                            .clickable { if (s.isRunning) vm.stopSession() else vm.startSession() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (s.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint     = if (s.isRunning) AccentRed else Accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        if (s.isRunning) "اضغط للإيقاف" else "اضغط للبدء",
                        color = TextMuted, fontSize = 13.sp
                    )
                }
            }
        }

        // ── Sessions ──────────────────────────────────────────────
        if (s.recentSessions.isNotEmpty()) {
            HorizontalDivider(color = Divider, thickness = 0.5.dp)
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "الجلسات الأخيرة",
                    color = TextMuted, fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                s.recentSessions.forEachIndexed { i, session ->
                    if (i > 0) HorizontalDivider(color = Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                    SessionRow(
                        session  = session,
                        onClick  = { onSessionClick(session) },
                        onDelete = { vm.deleteSession(session) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SessionRow(session: StudySession, onClick: () -> Unit, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(session.subject, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                "${fmt.format(Date(session.startMs))} · ${session.durationMin} دقيقة",
                color = TextMuted, fontSize = 12.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ChevronRight, null, tint = Surface3, modifier = Modifier.size(16.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
