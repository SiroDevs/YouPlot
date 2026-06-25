package com.you.plot.feature.plan.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

const val REMINDER_CHANNEL_ID = "youplot_reminders"
const val REMINDER_CHANNEL_NAME = "Plan Reminders"

const val KEY_TITLE = "reminder_title"
const val KEY_MESSAGE = "reminder_message"
const val KEY_PLAN_ID = "plan_id"

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "YouPlot Reminder"
        val message = inputData.getString(KEY_MESSAGE) ?: "Your activity is coming up!"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    REMINDER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                )
            )
        }

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
        return Result.success()
    }
}

data class PlanReminder(
    val tag: String,
    val title: String,
    val message: String,
    val fireAtMillis: Long,
)

fun scheduleReminder(context: Context, reminder: PlanReminder) {
    val delayMs = (reminder.fireAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    val data = Data.Builder()
        .putString(KEY_TITLE, reminder.title)
        .putString(KEY_MESSAGE, reminder.message)
        .build()
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(reminder.tag)
        .build()
    WorkManager.getInstance(context).enqueue(request)
}

fun cancelReminder(context: Context, tag: String) {
    WorkManager.getInstance(context).cancelAllWorkByTag(tag)
}

fun reminderTag(planId: Long, reminderIndex: Int) = "reminder_${planId}_$reminderIndex"
