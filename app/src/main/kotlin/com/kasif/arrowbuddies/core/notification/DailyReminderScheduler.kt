package com.kasif.arrowbuddies.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {

    private const val WORK_NAME = "arrow_buddies_daily_reminder"
    private const val TARGET_HOUR = 18 // 6:00 PM
    private const val TARGET_MINUTE = 0

    fun scheduleDailyReminder(context: Context) {
        val now = LocalDateTime.now()
        var targetTime = now.with(LocalTime.of(TARGET_HOUR, TARGET_MINUTE, 0))
        if (!now.isBefore(targetTime)) {
            // Already passed 6:00 PM today, schedule for tomorrow
            targetTime = targetTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, targetTime).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

