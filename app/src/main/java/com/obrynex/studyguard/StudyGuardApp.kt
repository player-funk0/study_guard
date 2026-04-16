package com.obrynex.studyguard

import android.app.Application
import com.obrynex.studyguard.di.ServiceLocator
import com.obrynex.studyguard.notifications.NotificationHelper

class StudyGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        NotificationHelper.createChannel(this)
    }
}
