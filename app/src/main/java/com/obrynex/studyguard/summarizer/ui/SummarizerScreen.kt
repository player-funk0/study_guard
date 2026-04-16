package com.obrynex.studyguard.summarizer.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.ui.theme.*

@Composable
fun SummarizerScreen(vm: SummarizerViewModel) {
    val s    by vm.state.collectAsState()
    val clip = LocalClipboardManager.current

    Column(
        Modifier.fillMaxSize().background(BgDark).verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("تلخيص", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Medium)
            AnimatedVisibility(s.result != null) {
                TextButton(onClick = vm::onReset, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("مسح", color = TextMuted, fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // Level selector
            Row(
                Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(6.dp)).padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryLevel.values().forEach { lvl ->
                    val selected = s.level == lvl
                    Box(
                        Modifier.weight(1f)
                            .background(if (selected) Surface3 else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { vm.onLevelChanged(lvl) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lvl.label, color = if (selected) TextPrimary else TextMuted, fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }

            OutlinedTextField(
                value = s.input, onValueChange = vm::onInputChanged,
                modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                placeholder = { Text("الصق النص هنا…", color = TextMuted, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent.copy(alpha = 0.6f), unfocusedBorderColor = Surface2,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Accent,
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp)
            )
            if (s.input.isNotBlank()) {
                Text("${s.input.trim().split(Regex("\\s+")).size} كلمة", color = TextMuted, fontSize = 11.sp)
            }

            // Two buttons: TextRank (fast) + AI (smart)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Fast TextRank button
                OutlinedButton(
                    onClick  = vm::onSummarizeClicked,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(6.dp),
                    enabled  = s.canSummarize,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border   = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
                ) {
                    Text("تلخيص سريع", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                // Gemma AI button — only shown if model is present
                if (s.hasAiModel) {
                    Button(
                        onClick  = vm::onAiSummarizeClicked,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(6.dp),
                        enabled  = s.canSummarize && !s.isAiLoading,
                        colors   = ButtonDefaults.buttonColors(containerColor = Accent, disabledContainerColor = Surface2)
                    ) {
                        if (s.isAiLoading) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                        } else {
                            Text("Gemma AI", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = s.result != null, enter = fadeIn() + expandVertically()) {
            Column {
                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("الملخص", color = TextMuted, fontSize = 12.sp)
                            if (s.isAiLoading) {
                                CircularProgressIndicator(Modifier.size(10.dp), color = Accent, strokeWidth = 1.dp)
                            }
                        }
                        IconButton(onClick = { clip.setText(AnnotatedString(s.result ?: "")) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                        }
                    }
                    Text(s.result ?: "", color = TextPrimary, fontSize = 15.sp, lineHeight = 26.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
