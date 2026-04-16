package com.obrynex.studyguard.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.ui.theme.*

private data class OnboardingPage(
    val icon       : ImageVector,
    val iconColor  : androidx.compose.ui.graphics.Color,
    val title      : String,
    val description: String
)

private val PAGES = listOf(
    OnboardingPage(
        icon        = Icons.Default.Timer,
        iconColor   = AccentAmber,
        title       = "تتبع وقت مذاكرتك",
        description = "ابدأ وأوقف جلسات المذاكرة بضغطة واحدة، وحدد هدفك اليومي وحافظ على تسلسل أيام المذاكرة."
    ),
    OnboardingPage(
        icon        = Icons.Default.AutoStories,
        iconColor   = Accent,
        title       = "لخّص أي نص بسهولة",
        description = "الصق أي نص دراسي واحصل على ملخص فوري. يعمل بدون إنترنت في أي وقت ومن أي مكان."
    ),
    OnboardingPage(
        icon        = Icons.Default.PhoneAndroid,
        iconColor   = AccentGreen,
        title       = "راقب وقت شاشتك",
        description = "اعرف كم تقضي على هاتفك يومياً، وحدد حداً لنفسك حتى تتوازن بين الدراسة والترفيه."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (page < PAGES.lastIndex) {
                TextButton(onClick = onFinish) {
                    Text("تخطى", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                Spacer(Modifier.height(36.dp))
            }
        }

        // Icon
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it }).togetherWith(
                    fadeOut() + slideOutHorizontally { -it }
                )
            }
        ) { idx ->
            Box(
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PAGES[idx].iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PAGES[idx].icon, null,
                    tint     = PAGES[idx].iconColor,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Text
        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
        ) { idx ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    PAGES[idx].title,
                    color      = TextPrimary,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                Text(
                    PAGES[idx].description,
                    color      = TextMuted,
                    fontSize   = 15.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PAGES.indices.forEach { i ->
                val active = i == page
                Box(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Accent else Surface3)
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { if (page < PAGES.lastIndex) page++ else onFinish() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text(
                if (page < PAGES.lastIndex) "التالي" else "ابدأ الآن",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
