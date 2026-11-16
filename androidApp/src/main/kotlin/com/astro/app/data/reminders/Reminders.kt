package com.astro.app.data.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.astro.app.MainActivity
import com.astro.app.R
import com.astro.app.core.AppConstants
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

// Morning/evening test reminder: a notification, then re-schedules the next slot.
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Reminders.notify(applicationContext)
        Reminders.scheduleNext(applicationContext)
        return Result.success()
    }
}

object Reminders {

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            AppConstants.REMINDER_CHANNEL_ID, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_HIGH,
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun scheduleNext(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayToNextSlot(now).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(AppConstants.REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun delayToNextSlot(now: LocalDateTime): Duration {
        val next = AppConstants.REMINDER_HOURS
            .map { now.toLocalDate().atTime(it, 0) }
            .firstOrNull { it.isAfter(now) }
            ?: now.toLocalDate().plusDays(1).atTime(AppConstants.REMINDER_HOURS.first(), 0)
        return Duration.between(now, next)
    }

    fun notify(context: Context) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return
        val intent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, AppConstants.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.reminder_text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(AppConstants.REMINDER_NOTIFICATION_ID, notification)
    }
}
