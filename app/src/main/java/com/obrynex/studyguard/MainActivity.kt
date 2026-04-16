package com.obrynex.studyguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.obrynex.studyguard.navigation.NavGraph
import com.obrynex.studyguard.notifications.ReminderScheduler
import com.obrynex.studyguard.ui.theme.BgDark
import com.obrynex.studyguard.ui.theme.StudyGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderScheduler.scheduleDailyReminder(this, 20, 0)
        setContent {
            StudyGuardTheme {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().background(BgDark)
                ) {
                    NavGraph()
                }
            }
        }
    }
}
