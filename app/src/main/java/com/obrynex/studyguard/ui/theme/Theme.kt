package com.obrynex.studyguard.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────

val BgDark      = Color(0xFF0D0C10)   // deep, slightly warm
val Surface1    = Color(0xFF141218)   // cards
val Surface2    = Color(0xFF1B1921)   // elevated / chips
val Surface3    = Color(0xFF211F2B)   // selected states
val Accent      = Color(0xFF7B6FE0)   // muted purple
val AccentGreen = Color(0xFF3CBDA2)
val AccentAmber = Color(0xFFD9A03C)
val AccentRed   = Color(0xFFD45C5C)
val TextPrimary = Color(0xFFEBEAF2)
val TextMuted   = Color(0xFF5C596F)
val Divider     = Color(0xFF1B1922)

private val darkScheme = darkColorScheme(
    primary          = Accent,
    onPrimary        = Color.White,
    secondary        = AccentGreen,
    onSecondary      = Color.Black,
    background       = BgDark,
    onBackground     = TextPrimary,
    surface          = Surface1,
    onSurface        = TextPrimary,
    surfaceVariant   = Surface2,
    onSurfaceVariant = TextMuted,
    error            = AccentRed,
    outline          = Surface3
)

@Composable
fun StudyGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkScheme, content = content)
}
