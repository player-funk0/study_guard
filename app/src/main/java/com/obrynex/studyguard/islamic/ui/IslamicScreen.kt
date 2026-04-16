package com.obrynex.studyguard.islamic.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.islamic.domain.model.*
import com.obrynex.studyguard.ui.theme.*

@Composable
fun IslamicScreen(vm: IslamicViewModel) {
    val s by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().background(BgDark)) {

        // ── Header ────────────────────────────────────────────────
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
            Text("الأحاديث والأذكار", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("من الكتب الستة الصحيحة", color = TextMuted, fontSize = 12.sp)
        }

        // ── Tabs ──────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Surface1, RoundedCornerShape(6.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IslamicTab.values().forEach { tab ->
                val active = tab == s.activeTab
                TextButton(
                    onClick  = { vm.onTabChanged(tab) },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(4.dp),
                    colors   = ButtonDefaults.textButtonColors(
                        containerColor = if (active) Accent else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor   = if (active) androidx.compose.ui.graphics.Color.White else TextMuted
                    )
                ) {
                    Text("${tab.emoji} ${tab.label}", fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Content ───────────────────────────────────────────────
        when (s.activeTab) {
            IslamicTab.HADITHS -> HadithsTab(s, vm)
            IslamicTab.ADHKAR  -> AdhkarTab(s, vm)
            IslamicTab.TASBIH  -> TasbihTab(s, vm)
        }
    }
}

// ── Hadiths Tab ───────────────────────────────────────────────────────────────

@Composable
private fun HadithsTab(s: IslamicUiState, vm: IslamicViewModel) {
    Column(Modifier.fillMaxSize()) {
        // Category filter chips
        val cats = listOf(null) + HadithCategory.values().toList()
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cats.forEach { cat ->
                val selected = s.hadithCategory == cat
                FilterChip(
                    selected = selected,
                    onClick  = { vm.onHadithCategoryChanged(cat) },
                    label    = { Text(if (cat == null) "الكل" else "${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent,
                        selectedLabelColor     = androidx.compose.ui.graphics.Color.White,
                        containerColor         = Surface2,
                        labelColor             = TextMuted
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            s.filteredHadiths.forEach { hadith ->
                HadithCard(
                    hadith   = hadith,
                    expanded = s.expandedHadithId == hadith.id,
                    onClick  = { vm.toggleHadith(hadith.id) }
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HadithCard(hadith: Hadith, expanded: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = Surface1)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Top row: category + grade
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(hadith.category.emoji, fontSize = 14.sp)
                    Text(hadith.category.label, color = TextMuted, fontSize = 11.sp)
                }
                GradeBadge(hadith.grade)
            }

            Spacer(Modifier.height(10.dp))

            // Arabic text — RTL
            Text(
                text      = hadith.arabicText,
                color     = TextPrimary,
                fontSize  = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
                textAlign = TextAlign.Right,
                style     = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Narrator
            Text(
                text      = hadith.narrator,
                color     = Accent,
                fontSize  = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            // Expanded section: translation + isnad + source
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    HorizontalDivider(color = Surface2)

                    // Translation
                    Text("المعنى", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(hadith.translation, color = TextPrimary, fontSize = 13.sp, lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth())

                    HorizontalDivider(color = Surface2)

                    // Isnad
                    Text("السند", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Card(
                        shape  = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface2)
                    ) {
                        Text(
                            text      = hadith.isnad + " — " + hadith.arabicText,
                            color     = TextPrimary,
                            fontSize  = 12.sp,
                            lineHeight = 22.sp,
                            modifier  = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = TextAlign.Right,
                            style     = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl)
                        )
                    }

                    // Source
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${hadith.source}  •  ${hadith.bookNumber}",
                            color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Right)
                    }
                }
            }

            // Expand hint
            Row(Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = TextMuted, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GradeBadge(grade: HadithGrade) {
    val color = androidx.compose.ui.graphics.Color(grade.color)
    Box(
        Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(grade.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Adhkar Tab ────────────────────────────────────────────────────────────────

@Composable
private fun AdhkarTab(s: IslamicUiState, vm: IslamicViewModel) {
    Column(Modifier.fillMaxSize()) {
        val cats = listOf(null) + DhikrCategory.values().toList()
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cats.forEach { cat ->
                val selected = s.dhikrCategory == cat
                FilterChip(
                    selected = selected,
                    onClick  = { vm.onDhikrCategoryChanged(cat) },
                    label    = { Text(if (cat == null) "الكل" else "${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen,
                        selectedLabelColor     = androidx.compose.ui.graphics.Color.White,
                        containerColor         = Surface2,
                        labelColor             = TextMuted
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            s.filteredAdhkar.forEach { dhikr ->
                DhikrCard(
                    dhikr    = dhikr,
                    expanded = s.expandedDhikrId == dhikr.id,
                    onClick  = { vm.toggleDhikr(dhikr.id) }
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DhikrCard(dhikr: Dhikr, expanded: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = Surface1)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(dhikr.category.emoji, fontSize = 14.sp)
                    Text(dhikr.category.label, color = TextMuted, fontSize = 11.sp)
                }
                Box(
                    Modifier
                        .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("× ${dhikr.repetitions}", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                dhikr.arabicText,
                color      = TextPrimary,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
                textAlign  = TextAlign.Right,
                style      = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                modifier   = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = Surface2)

                    Text("المعنى", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(dhikr.meaning, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp,
                        textAlign = TextAlign.Right,
                        style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth())

                    Text("الفضل", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(dhikr.virtue, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp,
                        textAlign = TextAlign.Right,
                        style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth())

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Icon(Icons.Default.MenuBook, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(dhikr.source, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = TextMuted, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Tasbih Tab ────────────────────────────────────────────────────────────────

@Composable
private fun TasbihTab(s: IslamicUiState, vm: IslamicViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tasbih selector
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            s.tasbihItems.forEach { item ->
                val selected = item.id == s.tasbihItemId
                FilterChip(
                    selected = selected,
                    onClick  = { vm.onTasbihItemSelected(item.id) },
                    label    = { Text(item.text, fontSize = 11.sp, textAlign = TextAlign.Right) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent,
                        selectedLabelColor     = androidx.compose.ui.graphics.Color.White,
                        containerColor         = Surface2,
                        labelColor             = TextMuted
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Counter circle
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress    = { s.tasbihProgress },
                modifier    = Modifier.size(220.dp),
                color       = if (s.tasbihDone) AccentGreen else Accent,
                trackColor  = Surface2,
                strokeWidth = 10.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    s.tasbihCount.toString(),
                    color      = if (s.tasbihDone) AccentGreen else TextPrimary,
                    fontSize   = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "/ ${s.currentTasbih.target}",
                    color    = TextMuted,
                    fontSize = 16.sp
                )
            }
        }

        // Current tasbih text
        Text(
            s.currentTasbih.text,
            color      = TextPrimary,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center,
            lineHeight = 34.sp,
            style      = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl)
        )

        if (s.tasbihDone) {
            Text("اكتمل! جزاك الله خيراً ✅", color = AccentGreen,
                fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Text(s.currentTasbih.virtue, color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)

        // Tap button
        Button(
            onClick  = vm::onTasbihTap,
            modifier = Modifier.size(90.dp),
            shape    = CircleShape,
            enabled  = !s.tasbihDone,
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (s.tasbihDone) AccentGreen else Accent,
                disabledContainerColor = AccentGreen
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("سبّح", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White)
        }

        // Reset
        TextButton(onClick = vm::onTasbihReset) {
            Icon(Icons.Default.Refresh, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("إعادة", color = TextMuted, fontSize = 13.sp)
        }

        Spacer(Modifier.height(80.dp))
    }
}
