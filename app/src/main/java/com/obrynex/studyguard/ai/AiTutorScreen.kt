package com.obrynex.studyguard.ai

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.obrynex.studyguard.ui.theme.*

@Composable
fun AiTutorScreen(vm: AiTutorViewModel) {
    val s by vm.state.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(s.messages.size) {
        if (s.messages.isNotEmpty()) listState.animateScrollToItem(s.messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(BgDark)) {

        // ── Header ────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("مساعد الدراسة", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(if (s.isModelReady) AccentGreen else TextMuted))
                    Text(
                        if (s.isModelReady) "Gemma 2B · محلي" else "النموذج غير موجود",
                        color = TextMuted, fontSize = 11.sp
                    )
                }
            }
            if (s.messages.isNotEmpty()) {
                TextButton(onClick = vm::clear, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("مسح", color = TextMuted, fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Model not ready ───────────────────────────────────────
        if (!s.isModelReady) {
            ModelSetupBanner(modelPath = s.modelPath)
            return@Column
        }

        // ── Subject chip ──────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("المادة:", color = TextMuted, fontSize = 12.sp)
            listOf("عام", "رياضيات", "فيزياء", "كيمياء", "أحياء", "تاريخ").forEach { sub ->
                val active = s.subject == sub || (sub == "عام" && s.subject.isEmpty())
                Box(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Surface3 else Surface1)
                        .clickable { vm.onSubjectChanged(if (sub == "عام") "" else sub) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(sub, color = if (active) TextPrimary else TextMuted, fontSize = 11.sp)
                }
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Chat messages ─────────────────────────────────────────
        if (s.messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("اسألني أي سؤال", color = TextMuted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("الذكاء الاصطناعي يعمل على جهازك بالكامل", color = TextMuted.copy(0.5f), fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                state            = listState,
                modifier         = Modifier.weight(1f),
                contentPadding   = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(s.messages) { msg ->
                    MessageBubble(msg)
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────
        HorizontalDivider(color = Divider, thickness = 0.5.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value         = s.input,
                onValueChange = vm::onInputChanged,
                modifier      = Modifier.weight(1f),
                placeholder   = { Text("اكتب سؤالك…", color = TextMuted, fontSize = 13.sp) },
                maxLines      = 4,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Accent.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Surface2,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = Accent,
                    focusedContainerColor   = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (s.input.isNotBlank() && !s.isGenerating) Accent
                        else Surface2
                    )
                    .clickable(enabled = s.input.isNotBlank() && !s.isGenerating) { vm.send() },
                contentAlignment = Alignment.Center
            ) {
                if (s.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color    = TextMuted,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send, null,
                        tint     = if (s.input.isNotBlank()) androidx.compose.ui.graphics.Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart    = if (msg.isUser) 14.dp else 4.dp,
                        topEnd      = if (msg.isUser) 4.dp  else 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd   = 14.dp
                    )
                )
                .background(if (msg.isUser) Accent.copy(alpha = 0.15f) else Surface1)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (msg.isLoading) {
                LoadingDots()
            } else {
                Text(
                    msg.text,
                    color      = TextPrimary,
                    fontSize   = 14.sp,
                    lineHeight  = 22.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val inf = rememberInfiniteTransition(label = "dots")
    val alpha by inf.animateFloat(
        initialValue    = 0.3f, targetValue = 1f,
        animationSpec   = infiniteRepeatable(
            animation  = androidx.compose.animation.core.tween(600),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "a"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp))
                .background(TextMuted.copy(alpha = alpha - it * 0.1f)))
        }
    }
}

@Composable
private fun ModelSetupBanner(modelPath: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("إعداد الذكاء الاصطناعي", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        Text(
            "النموذج يعمل 100% على جهازك — بدون إنترنت ولا أي تكلفة.",
            color = TextMuted, fontSize = 13.sp, lineHeight = 22.sp
        )

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        Text("خطوات الإعداد (مرة واحدة فقط):", color = TextMuted, fontSize = 12.sp)

        listOf(
            "1" to "روح kaggle.com/models/google/gemma/tfLite/gemma-2b-it-cpu-int4",
            "2" to "حمّل ملف gemma-2b-it-cpu-int4.bin (حجمه ~1.3 جيجا)",
            "3" to "انسخه على جهازك بالـ USB أو adb:"
        ).forEach { (num, step) ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(num, color = Accent.copy(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(step, color = TextMuted, fontSize = 12.sp, lineHeight = 20.sp)
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Surface1)
                .padding(12.dp)
        ) {
            Text(
                "adb push gemma-2b-it-cpu-int4.bin \"$modelPath\"",
                color      = AccentGreen,
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            "بعد النسخ، اقفل التطبيق وافتحه تاني.",
            color = TextMuted, fontSize = 12.sp
        )
    }
}
