package com.obrynex.studyguard.wellbeing

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.ui.theme.*

@Composable
fun WellbeingScreen(vm: WellbeingViewModel) {
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
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("الشاشة", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = vm::refresh, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Refresh, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        if (!s.hasPermission) {
            PermissionSection(onClick = vm::openUsageSettings)
            return@Column
        }

        // ── Screen time numbers ───────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val h = s.totalScreenMin / 60; val m = s.totalScreenMin % 60
                Text(
                    if (h > 0) "${h}:${"%02d".format(m)}" else "$m",
                    color = if (s.overLimit) AccentRed else TextPrimary,
                    fontSize = 48.sp, fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Text(if (s.totalScreenMin / 60 > 0) "ساعة:دقيقة اليوم" else "دقيقة اليوم", color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${s.limitHr.toInt()}",
                    color = AccentAmber, fontSize = 22.sp, fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Text("الحد (ساعات)", color = TextMuted, fontSize = 11.sp)
            }
        }

        // Thin usage bar
        val usagePct = (s.addictionScore / 100f).coerceIn(0f, 1f)
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LinearProgressIndicator(
                progress   = { usagePct },
                modifier   = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color      = Color(s.addictionColor),
                trackColor = Surface2
            )
            Text("${s.addictionScore}% · ${s.addictionLabel}", color = TextMuted, fontSize = 11.sp)
        }

        if (s.overLimit) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Warning, null, tint = AccentRed, modifier = Modifier.size(14.dp))
                Text("تجاوزت الحد اليومي", color = AccentRed, fontSize = 13.sp)
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(top = if (s.overLimit) 0.dp else 16.dp))

        // ── Limit slider ──────────────────────────────────────────
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            var sliderVal by remember(s.limitHr) { mutableStateOf(s.limitHr) }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("الحد اليومي", color = TextMuted, fontSize = 12.sp)
                Text("${sliderVal.toInt()} ساعة", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Slider(
                value = sliderVal, onValueChange = { sliderVal = it.toInt().toFloat() },
                onValueChangeFinished = { vm.updateLimit(sliderVal) },
                valueRange = 1f..12f, steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = AccentAmber,
                    activeTrackColor = AccentAmber,
                    inactiveTrackColor = Surface2
                ),
                modifier = Modifier.padding(top = 0.dp)
            )
        }

        // ── Top apps ──────────────────────────────────────────────
        if (s.topApps.isNotEmpty()) {
            HorizontalDivider(color = Divider, thickness = 0.5.dp)
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text("التطبيقات", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                s.topApps.forEachIndexed { i, app ->
                    if (i > 0) HorizontalDivider(color = Divider.copy(alpha = 0.6f), thickness = 0.5.dp)
                    AppRow(app = app, max = s.topApps.first().minutesUsed, rank = i + 1)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionSection(onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.PhoneAndroid, null, tint = TextMuted, modifier = Modifier.size(32.dp))
        Text("يحتاج إذن الوصول للإحصائيات", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(
            "بياناتك تبقى على جهازك فقط. لن يُرسل أي شيء.",
            color = TextMuted, fontSize = 13.sp
        )
        OutlinedButton(
            onClick = onClick,
            shape  = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Accent)
        ) {
            Text("منح الإذن", fontSize = 14.sp)
        }
    }
}

@Composable
private fun AppRow(app: AppUsage, max: Int, rank: Int) {
    val pct = app.minutesUsed.toFloat() / max.toFloat()
    Column(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$rank", color = TextMuted, fontSize = 11.sp)
                Text(app.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            val h = app.minutesUsed / 60; val m = app.minutesUsed % 60
            Text(
                if (h > 0) "${h}س ${m}د" else "${m}د",
                color = TextMuted, fontSize = 12.sp
            )
        }
        LinearProgressIndicator(
            progress   = { pct },
            modifier   = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color      = if (rank == 1) AccentRed.copy(alpha = 0.7f) else Accent.copy(alpha = 0.5f),
            trackColor = Surface2
        )
    }
}
