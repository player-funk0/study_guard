package com.obrynex.studyguard.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

// ── Channel ───────────────────────────────────────────────────────────────────

object NotificationHelper {

    const val CHANNEL_ID   = "studyguard_reminders"
    const val CHANNEL_NAME = "تذكيرات المذاكرة"

    fun createChannel(ctx: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "تذكيرات يومية للمذاكرة ووقت الشاشة" }
        ctx.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun sendNotification(ctx: Context, title: String, body: String, id: Int = 1) {
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(id, notif)
    }
}

// ── Study Reminder Worker ─────────────────────────────────────────────────────

class StudyReminderWorker(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        NotificationHelper.sendNotification(
            ctx,
            title = "📚 وقت المذاكرة!",
            body  = "افتح StudyGuard وابدأ جلسة جديدة. حافظ على تسلسلك! 🔥",
            id    = 101
        )
        return Result.success()
    }
}

// ── Screen Time Warning Worker ────────────────────────────────────────────────

class ScreenTimeWorker(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val limitHr = inputData.getFloat("limit_hr", 4f)
        NotificationHelper.sendNotification(
            ctx,
            title = "📵 تحذير وقت الشاشة",
            body  = "اقتربت من حدك اليومي (${limitHr} ساعة). ضع الهاتف جانباً.",
            id    = 102
        )
        return Result.success()
    }
}

// ── Scheduler ─────────────────────────────────────────────────────────────────

object ReminderScheduler {

    private const val STUDY_TAG  = "study_reminder"
    private const val SCREEN_TAG = "screen_warning"

    fun scheduleDailyReminder(ctx: Context, hour: Int, minute: Int) {
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= now) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val delayMs = cal.timeInMillis - now

        val request = PeriodicWorkRequestBuilder<StudyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(STUDY_TAG)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            STUDY_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleScreenWarning(ctx: Context, delayMin: Long, limitHr: Float) {
        val data = workDataOf("limit_hr" to limitHr)
        val req  = OneTimeWorkRequestBuilder<ScreenTimeWorker>()
            .setInitialDelay(delayMin, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag(SCREEN_TAG)
            .build()

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            SCREEN_TAG,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    fun cancelAll(ctx: Context) = WorkManager.getInstance(ctx).cancelAllWork()
}
